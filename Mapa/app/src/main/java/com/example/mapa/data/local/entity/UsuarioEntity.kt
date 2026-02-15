package com.example.mapa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mapa.data.remote.dto.Usuario

@Entity(tableName = "usuario")
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val nome: String?,
    val email: String?,
    val fotoUrl: String?
)