package com.example.mapa.data.remote.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um chat no banco de dados remoto.
 */
@Parcelize
data class ChatDTO (
    val salaId: String = "",
    val ultimaMsg: MensagemDTO? = null,
    val ultimoTimestamp: Long = System.currentTimeMillis(),
    val participantes: List<String> = emptyList(),
    val visivelPara: List<String> = emptyList(),
    val localId: String = ""
) : Parcelable