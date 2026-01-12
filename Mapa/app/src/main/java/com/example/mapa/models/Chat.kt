package com.example.mapa.models

/**
 * Classe que representa um chat
 */
data class Chat (
    val salaId: String = "",
    val ultimaMsg: Mensagem = Mensagem(),
    val ultimoTimestamp: Long = System.currentTimeMillis(),
    val participantes: List<String> = emptyList()
)