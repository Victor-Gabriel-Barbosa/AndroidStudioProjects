package com.example.mapa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa um local no banco de dados local.
 */
@Entity(tableName = "location")
data class LocationEntity(
    @PrimaryKey val id: String,
    val uid: String,
    val name: String,
    val type: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
    val date: Long?,
    val imgUrls: String
)