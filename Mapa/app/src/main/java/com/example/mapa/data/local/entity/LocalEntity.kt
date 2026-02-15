package com.example.mapa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "locais")
data class LocalEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val nome: String,
    val tipo: String,
    val descricao: String,
    val latitude: Double,
    val longitude: Double,
    val raio: Double,
    val data: Long?,
    val imgUrls: String
)