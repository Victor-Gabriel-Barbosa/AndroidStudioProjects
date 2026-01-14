package com.example.mapa.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um chat
 */
@Parcelize
data class Chat (
    val salaId: String = "",
    val ultimaMsg: Mensagem = Mensagem(),
    val ultimoTimestamp: Long = System.currentTimeMillis(),
    val participantes: List<String> = emptyList()
) : Parcelable