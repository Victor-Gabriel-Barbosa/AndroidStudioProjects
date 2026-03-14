package com.example.mapa.data.remote.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa uma mensagem no banco de dados remoto.
 */
@Parcelize
data class MsgDTO (
    val id: String = "",
    val text: String = "",
    val uid: String = "",
    val read: Boolean = false,
    val edited: Boolean = false,
    val imgUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable