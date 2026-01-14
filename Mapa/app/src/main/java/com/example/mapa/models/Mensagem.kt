package com.example.mapa.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa uma mensagem
 */
@Parcelize
data class Mensagem (
    val id: String = "",
    val texto: String = "",
    val remetenteUid: String = "",
    val destinatarioUid: String = "",
    val lido: Boolean = false,
    val editado: Boolean = false,
    val imgUrls: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable