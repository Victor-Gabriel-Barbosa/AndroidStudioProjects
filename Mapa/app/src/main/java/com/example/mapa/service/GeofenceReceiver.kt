package com.example.mapa.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mapa.MainActivity
import com.example.mapa.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlin.random.Random

/**
 * BroadcastReceiver para lidar com eventos de geofence.
 *
 * Este receiver é acionado quando o dispositivo entra em uma área de geofence monitorada,
 * exibindo uma notificação para o usuário.
 */
class GeofenceReceiver : BroadcastReceiver() {
    /**
     * Este método é chamado quando o BroadcastReceiver está recebendo um Intent broadcast.
     *
     * @param context O Contexto no qual o receiver está rodando.
     * @param intent O Intent sendo recebido.
     */
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
        if (geofencingEvent.hasError()) return

        if (geofencingEvent.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return

        val geofences = geofencingEvent.triggeringGeofences ?: return

        // Acessa o nosso cache local de nomes
        val prefs = context.getSharedPreferences("geofence_nomes_cache", Context.MODE_PRIVATE)

        geofences.forEach { geofence ->
            val localId = geofence.requestId

            // Busca o nome pelo ID. Se não achar, exibe "um item marcado"
            val localNome = prefs.getString(localId, "um item marcado") ?: "um item marcado"

            mostrarNotificacao(
                context = context,
                titulo = "Você está perto de um item perdido",
                corpo = "Você está próximo de: $localNome"
            )
        }
    }

    /**
     * Cria e exibe uma notificação no dispositivo.
     *
     * @param context O contexto da aplicação.
     * @param titulo O título da notificação.
     * @param corpo O corpo da notificação.
     */
    private fun mostrarNotificacao(context: Context, titulo: String, corpo: String) {
        val channelId = "geofence_channel"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de proximidade",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setStyle(NotificationCompat.BigTextStyle().bigText(corpo))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }
}