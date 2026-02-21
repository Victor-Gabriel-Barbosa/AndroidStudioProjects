package com.example.mapa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa um usuário no banco de dados local.
 */
@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val nome: String?,
    val email: String?,
    val fotoUrl: String?,
    val notaMedia: Double,
    val notaQtd: Int,
    val avaliadores: String,
    val fcmToken: String
)