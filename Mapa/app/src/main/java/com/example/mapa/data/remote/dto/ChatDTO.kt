package com.example.mapa.data.remote.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um chat no banco de dados remoto.
 */
@Parcelize
data class ChatDTO (
    val id: String = "",
    val lastMsg: MsgDTO? = null,
    val lastTimestamp: Long = System.currentTimeMillis(),
    val participants: List<String> = emptyList(),
    val visibleTo: List<String> = emptyList(),
    val locationId: String = ""
) : Parcelable