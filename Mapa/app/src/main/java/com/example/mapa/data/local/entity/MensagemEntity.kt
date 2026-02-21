package com.example.mapa.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidade que representa uma mensagem no banco de dados local.
 */
@Entity(tableName = "mensagem")
data class MensagemEntity(
    @PrimaryKey val id: String,
    val salaId: String,
    val autorUid: String,
    val texto: String,
    val timestamp: Long,
    val lido: Boolean,
    val imgUrls: String
)