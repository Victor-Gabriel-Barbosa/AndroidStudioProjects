package com.example.mapa.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.mapa.R
import com.example.mapa.data.remote.dto.LocalDTO
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import androidx.core.content.edit
import com.example.mapa.receiver.GeofenceReceiver

/**
 * Classe auxiliar para gerenciar Geofences.
 *
 * @property context O contexto da aplicação.
 */
class GeofenceService(private val context: Context) {
    private val geofencing = LocationServices.getGeofencingClient(context)

    // Nome do arquivo de preferências para salvar os IDs
    private val prefsGeofence = "geofence_prefs"
    private val ids = "ids_registrados"

    /**
     * Recupera os IDs que estão salvos no armazenamento persistente.
     */
    private fun getIdsRegistrados(): MutableSet<String> {
        val prefs = context.getSharedPreferences(prefsGeofence, Context.MODE_PRIVATE)
        return prefs.getStringSet(ids, emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    /**
     * Salva a nova lista de IDs no armazenamento persistente
     */
    private fun salvarIdsRegistrados(ids: Set<String>) {
        val prefs = context.getSharedPreferences(prefsGeofence, Context.MODE_PRIVATE)
        prefs.edit {
            putStringSet(this@GeofenceService.ids, ids)
        }
    }

    /**
     * Cria um PendingIntent para o BroadcastReceiver de Geofence.
     *
     * Este PendingIntent é usado para iniciar o BroadcastReceiver quando uma geofence é disparada.
     */
    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE
                else PendingIntent.FLAG_IMMUTABLE

        PendingIntent.getBroadcast(context, 1001, intent, flags)
    }

    /**
     * Registra geofences para locais marcados como "perdido".
     *
     * As geofences são criadas em torno das coordenadas de cada local.
     * Geofences antigas são removidas antes de adicionar novas.
     *
     * @param locais A lista de locais para registrar. Apenas locais do tipo "perdido"
     * e com coordenadas válidas serão registrados.
     */
    @SuppressLint("MissingPermission")
    fun registrarLocaisPerdidos(locais: List<LocalDTO>) {
        // Filtra os locais válidos do tipo "perdido"
        val locaisPerdidos = locais.filter {
            it.tipo == context.getString(R.string.perdido) && !(it.latitude == 0.0 && it.longitude == 0.0)
        }

        // Salva os nomes no SharedPreferences para o Receiver poder ler depois
        val prefsNomes = context.getSharedPreferences("geofence_nomes_cache", Context.MODE_PRIVATE)
        prefsNomes.edit {
            locaisPerdidos.forEach { local ->
                putString(local.id, local.nome)
            }
        }

        // Descobre os IDs que estão na lista atual
        val idsAtuais = locaisPerdidos.map { it.id }.toSet()
        val idsRegistrados = getIdsRegistrados()

        // Remove Geofences que não estão mais na lista atual
        val idsParaRemover = idsRegistrados.subtract(idsAtuais)
        if (idsParaRemover.isNotEmpty()) {
            geofencing.removeGeofences(idsParaRemover.toList())
                .addOnSuccessListener {
                    Log.d("GeofenceHelper", "Removidos: $idsParaRemover")
                    val novaLista = getIdsRegistrados()
                    novaLista.removeAll(idsParaRemover)
                    salvarIdsRegistrados(novaLista)
                }
        }

        val novosLocais = locaisPerdidos.filter { !idsRegistrados.contains(it.id) }
        if (novosLocais.isEmpty()) return

        Log.i("TAG", "registrarLocaisPerdidos: ${novosLocais.map { it.id }}")
        Log.i("TAG", "registrarLocaisPerdidos2: $idsRegistrados")

        // Cria as geofences para cada local
        val geofences = novosLocais.map { local ->
            Geofence.Builder()
                .setRequestId(local.id)
                .setCircularRegion(
                    local.latitude,
                    local.longitude,
                    local.raio.toFloat().coerceAtLeast(10f)
                )
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .setLoiteringDelay(10000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build()
        }

        // Cria a requisição de geofences
        val req = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        geofencing.addGeofences(req, pendingIntent)
            .addOnSuccessListener {
                Log.d("GeofenceHelper", "Registrados/Atualizados: ${idsAtuais.size}")
                salvarIdsRegistrados(idsAtuais)
            }
            .addOnFailureListener { e ->
                Log.e("GeofenceHelper", "Erro ao registrar", e)
            }
    }

    /**
     * Remove todas as geofences registradas.
     */
    fun removerTodas() {
        geofencing.removeGeofences(pendingIntent)
        salvarIdsRegistrados(emptySet())
    }
}