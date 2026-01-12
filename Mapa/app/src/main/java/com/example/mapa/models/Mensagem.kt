package com.example.mapa.models

/**
 * Classe que representa uma mensagem
 */
data class Mensagem (
    val id: String = "",
    val texto: String = "",
    val remetenteUid: String = "",
    val destinatarioUid: String = "",
    val lido: Boolean = false,
    val editado: Boolean = false,
    val imgUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)