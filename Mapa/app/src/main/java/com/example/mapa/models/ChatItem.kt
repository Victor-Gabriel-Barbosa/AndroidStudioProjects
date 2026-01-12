package com.example.mapa.models

/**
 * Classe que representa um item na lista de chats
 */
data class ChatItem(
    val chat: Chat,
    val destinatario: Usuario?
)