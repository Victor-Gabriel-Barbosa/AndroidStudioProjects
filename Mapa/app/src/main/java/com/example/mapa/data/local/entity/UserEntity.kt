package com.example.mapa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa um usuário no banco de dados local.
 */
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String?,
    val email: String?,
    val photoUrl: String?,
    val averageRating: Double,
    val ratingCount: Int,
    val reviewerUids: String,
    val fcmToken: String
)