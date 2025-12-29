package com.example.refindu.models

// Classe que representa um usuário do sistema
data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)