package com.example.mapa.data.remote.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa uma mensagem no banco de dados remoto.
 */
@Parcelize
data class MensagemDTO (
    val id: String = "",
    val texto: String = "",
    val autorUid: String = "",
    val lido: Boolean = false,
    val editado: Boolean = false,
    val imgUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable