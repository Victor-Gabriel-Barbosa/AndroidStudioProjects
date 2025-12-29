package com.example.refindu.viewmodels

import androidx.lifecycle.ViewModel
import com.example.refindu.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentRoomId: String? = null
    private var chatPartnerId: String? = null
    private var currentUserUid: String? = auth.currentUser?.uid

    fun initializeChat(otherUserId: String) {
        currentUserUid = auth.currentUser?.uid
        if (currentUserUid == null) return

        this.chatPartnerId = otherUserId
        this.currentRoomId = generateRoomId(currentUserUid!!, otherUserId)
        listenToMessages()
    }

    private fun listenToMessages() {
        val roomId = currentRoomId ?: return

        firestore.collection("chats")
            .document(roomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null) {
                    val messageList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Message::class.java)?.copy(id = doc.id)
                    }
                    _messages.value = messageList
                    markMessagesAsRead(messageList)
                }
            }
    }

    fun sendMessage(text: String) {
        val roomId = currentRoomId ?: return
        val partnerUid = chatPartnerId ?: return
        val myUid = currentUserUid ?: return

        if (text.isBlank()) return

        val newMessage = Message(
            text = text,
            senderUid = myUid,
            receiverUid = partnerUid,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        // 1. Salva a mensagem
        firestore.collection("chats")
            .document(roomId)
            .collection("messages")
            .add(newMessage)

        // 2. Atualiza o resumo da conversa (para aparecer na lista de Recentes)
        val summaryData = hashMapOf(
            "lastMessage" to text,
            "lastTimestamp" to newMessage.timestamp,
            "participants" to listOf(myUid, partnerUid)
        )
        // Usa merge para não apagar outros dados se existirem
        firestore.collection("chats").document(roomId).set(summaryData, SetOptions.merge())
    }

    private fun markMessagesAsRead(messages: List<Message>) {
        val roomId = currentRoomId ?: return
        val myUid = currentUserUid ?: return

        // Marca como lido tudo que não fui eu que mandei e ainda não está lido
        messages.filter { it.senderUid != myUid && !it.isRead }.forEach { message ->
            firestore.collection("chats")
                .document(roomId)
                .collection("messages")
                .document(message.id)
                .update("isRead", true)
        }
    }

    private fun generateRoomId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    fun getCurrentUserUid() = currentUserUid ?: ""
}