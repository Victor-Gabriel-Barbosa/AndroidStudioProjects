package com.example.mapa.models

/**
 * Classe que representa o estado do chat
 */
data class ChatUiState(
    val msgs: List<Mensagem> = emptyList(),
    val destinatario: Usuario? = null,
    val carregando: Boolean = false,
    val erro: String? = null
)