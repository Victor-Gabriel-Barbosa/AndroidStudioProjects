package com.example.mapa.models

/**
 * Classe que representa o estado da lista de chats
 */
data class ChatListUiState(
    val chats: List<ChatItem> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null
)