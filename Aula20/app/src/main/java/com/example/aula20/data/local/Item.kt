package com.example.aula20.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val descricao: String,
    val comprado: Boolean
)