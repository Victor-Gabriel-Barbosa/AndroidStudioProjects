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
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import androidx.core.content.edit

/**
 * Classe auxiliar para gerenciar Geofences.
 *
 * @property context O contexto da aplicação.
 */
class GeofenceHelper(private val context: Context) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    // Guarda os IDs das geofences que já foram registradas nesta sessão
    private val geofencesRegistradas = mutableSetOf<String>()

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_MUTABLE
                else
                    PendingIntent.FLAG_IMMUTABLE

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

        // Salve os nomes no SharedPreferences para o Receiver poder ler depois
        val prefs = context.getSharedPreferences("geofence_nomes_cache", Context.MODE_PRIVATE)
        prefs.edit {
            locaisPerdidos.forEach { local ->
                putString(local.id, local.nome)
            }
        }

        // Descobre os IDs que estão na lista atual
        val idsAtuais = locaisPerdidos.map { it.id }.toSet()

        // Verifica quais geofences foram apagadas (estão registradas, mas sumiram da lista atual)
        val idsParaRemover = geofencesRegistradas.subtract(idsAtuais)

        if (idsParaRemover.isNotEmpty()) {
            geofencingClient.removeGeofences(idsParaRemover.toList())
                .addOnSuccessListener {
                    Log.d("GeofenceHelper", "Geofences antigas removidas: $idsParaRemover")
                    geofencesRegistradas.removeAll(idsParaRemover)
                }
        }

        // Constrói a lista de Geofences para adicionar ou atualizar
        val geofences = locaisPerdidos.map { local ->
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

        if (geofences.isEmpty()) return

        val req = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofences)
            .build()

        // Adiciona (ou sobrepõe as que já existem com o mesmo ID)
        geofencingClient.addGeofences(req, pendingIntent)
            .addOnSuccessListener {
                Log.d("GeofenceHelper", "Geofences registradas/atualizadas: ${geofences.size}")
                geofencesRegistradas.addAll(idsAtuais)
            }
            .addOnFailureListener { e ->
                Log.e("GeofenceHelper", "Erro ao registrar geofences", e)
            }
    }

    /**
     * Remove todas as geofences registradas por este helper.
     */
    fun removerTodas() {
        geofencingClient.removeGeofences(pendingIntent)
        geofencesRegistradas.clear()
    }
}