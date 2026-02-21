package com.example.mapa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa chats no banco de dados local.
 */
@Entity(tableName = "chat")
data class ChatEntity(
    @PrimaryKey val salaId: String,
    val ultimoTimestamp: Long,
    val participantes: String,
    val visivelPara: String,
    val ultimaMsgAutorUid: String?,
    val ultimaMsgTexto: String?,
    val ultimaMsgTimestamp: Long?,
    val ultimaMsgLido: Boolean?,
    val localId: String
)