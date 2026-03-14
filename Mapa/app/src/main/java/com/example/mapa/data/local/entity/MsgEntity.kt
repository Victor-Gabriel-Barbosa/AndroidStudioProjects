package com.example.mapa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa uma mensagem no banco de dados local.
 */
@Entity(tableName = "msg")
data class MsgEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val uid: String,
    val text: String,
    val timestamp: Long,
    val read: Boolean,
    val imgUrls: String
)