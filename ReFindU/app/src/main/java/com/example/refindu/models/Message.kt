package com.example.refindu.models

// Classe que representa uma mensagem
data class Message(
    val id: String = "",
    val text: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)