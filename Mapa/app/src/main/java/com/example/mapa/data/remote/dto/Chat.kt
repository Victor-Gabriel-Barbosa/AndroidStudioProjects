package com.example.mapa.data.remote.dto

import android.os.Parcelable
import com.example.mapa.models.Mensagem
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um chat
 */
@Parcelize
data class Chat (
    val salaId: String = "",
    val ultimaMsg: Mensagem? = null,
    val ultimoTimestamp: Long = System.currentTimeMillis(),
    val participantes: List<String> = emptyList(),
    val visivelPara: List<String> = emptyList()
) : Parcelable