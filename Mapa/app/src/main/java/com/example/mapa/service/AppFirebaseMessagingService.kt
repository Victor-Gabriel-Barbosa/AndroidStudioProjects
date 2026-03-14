package com.example.mapa.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mapa.MainActivity
import com.example.mapa.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.net.HttpURLConnection
import java.net.URL
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
        val title = message.data["title"] ?: message.notification?.title ?: "Nova Mensagem"
        val body = message.data["body"] ?: message.notification?.body ?: ""

        // Extrai os dados customizados invisíveis atualizados (payload de data)
        val contactUid = message.data["contactUid"]
        val locationId = message.data["locationId"]
        val imgUrl = message.data["imageUrl"]

        // Passa os dados extraídos para a função que monta a tela
        showNotification(title, body, contactUid, locationId, imgUrl)
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
     * @param title O título da notificação.
     * @param body O corpo (texto principal) da notificação.
     * @param contactUid O ID do contato para navegação ao clicar na notificação (opcional).
     * @param locationId O ID do local para navegação ao clicar na notificação (opcional).
     */
    private fun showNotification(title: String, body: String, contactUid: String?, locationId: String?, imgUrl: String?) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "channel_chat"

        // O Android 8.0+ exige que as notificações pertençam a um Canal (Channel)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                channelId,
                "Mensagens",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de novas mensagens do chat"
            }
            notificationManager.createNotificationChannel(canal)
        }

        // Cria um Intent para abrir a MainActivity quando a notificação for clicada
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("contactUid", contactUid)
            putExtra("locationId", locationId)
        }

        // Encapsula o Intent em um PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val groupId = "chat_group_$contactUid"

        // Converte a URL em um Bitmap
        val bitmapImagem = getBitmapFromUrl(imgUrl)

        // Constrói a interface visual da notificação
        val builderIndividual = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setGroup(groupId)

        // Se baixou a imagem com sucesso, aplica o estilo BigPicture
        if (bitmapImagem != null) {
            builderIndividual.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmapImagem)
                    .setSummaryText(body)
            )
        }

        // Mostra a notificação individual com ID aleatório
        val msgId = Random.nextInt()
        notificationManager.notify(msgId, builderIndividual.build())

        // Constrói a notificação de Resumo (Summary)
        val summaryNotification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setStyle(NotificationCompat.InboxStyle()
            .setSummaryText(title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setGroup(groupId)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Mostra a notificação de Resumo com um ID único
        val summaryId = contactUid?.hashCode() ?: 1001
        notificationManager.notify(summaryId, summaryNotification)
    }

    /**
     * Função auxiliar para baixar a imagem da internet e converter em Bitmap.
     */
    private fun getBitmapFromUrl(imgUrl: String?): Bitmap? {
        if (imgUrl.isNullOrEmpty()) return null
        return try {
            val url = URL(imgUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}