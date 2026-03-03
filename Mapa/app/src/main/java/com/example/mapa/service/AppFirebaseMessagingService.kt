package com.example.mapa.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mapa.MainActivity
import com.example.mapa.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

/**
 * Serviço para gerenciar o recebimento de mensagens do Firebase Cloud Messaging (FCM).
 *
 * Esta classe é responsável por receber as notificações push, processar os dados
 * e exibir uma notificação no dispositivo do usuário.
 */
class AppFirebaseMessagingService : FirebaseMessagingService() {
    /**
     * Chamado quando uma nova mensagem FCM é recebida.
     *
     * @param message O objeto [RemoteMessage] contendo os dados da notificação e o payload de dados.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Extrai os dados visuais da notificação
        val titulo = message.notification?.title ?: "Nova Mensagem"
        val corpo = message.notification?.body ?: ""

        // Extrai os dados customizados invisíveis atualizados (payload de data)
        val contatoUid = message.data["contatoUid"]
        val localId = message.data["localId"]

        // Passa os dados extraídos para a função que monta a tela
        mostrarNotificacao(titulo, corpo, contatoUid, localId)
    }

    /**
     * Chamado quando um novo token de registro do FCM é gerado.
     *
     * Este token é usado para enviar mensagens para uma instância específica do aplicativo.
     *
     * @param token O novo token de registro.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    /**
     * Cria e exibe uma notificação no sistema.
     *
     * @param titulo O título da notificação.
     * @param corpo O corpo (texto principal) da notificação.
     * @param contatoUid O ID do contato para navegação ao clicar na notificação (opcional).
     * @param localId O ID do local para navegação ao clicar na notificação (opcional).
     */
    private fun mostrarNotificacao(titulo: String, corpo: String, contatoUid: String?, localId: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val canalId = "canal_chat"

        // O Android 8.0+ exige que as notificações pertençam a um Canal (Channel)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Mensagens",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de novas mensagens do chat"
            }
            notificationManager.createNotificationChannel(canal)
        }

        // Cria um Intent para abrir a MainActivity quando a notificação for clicada
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // Limpa a pilha de atividades
            putExtra("contatoUid", contatoUid)
            putExtra("localId", localId)
        }

        // Encapsula o Intent em um PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Constrói a interface visual da notificação
        val notificacao = NotificationCompat.Builder(this, canalId)
            .setSmallIcon(R.drawable.logo) // Ícone da notificação
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setAutoCancel(true) // Remove a notificação quando clicada
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Faz a notificação aparecer no topo da tela
            .setContentIntent(pendingIntent)
            .build()

        // Mostra a notificação usando um ID aleatório para não sobrescrever notificações anteriores
        notificationManager.notify(Random.nextInt(), notificacao)
    }
}