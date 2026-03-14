package com.example.mapa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa chats no banco de dados local.
 */
@Entity(tableName = "chat")
data class ChatEntity(
    @PrimaryKey val id: String,
    val lastTimestamp: Long,
    val participants: String,
    val visibleTo: String,
    val lastMsgUid: String?,
    val lastMsgText: String?,
    val lastMsgTimestamp: Long?,
    val lastMsgRead: Boolean?,
    val locationId: String
)