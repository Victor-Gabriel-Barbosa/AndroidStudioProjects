package com.example.mapa.models

/**
 * Classe que representa um usuário
 */
data class Usuario (
    val uid: String = "",
    val nome: String? = null,
    val email: String? = null,
    val foto: String? = null
)