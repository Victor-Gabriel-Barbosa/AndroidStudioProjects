package com.example.mapa.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado do chat
 */
@Parcelize
data class ChatUiState(
    val msgs: List<Mensagem> = emptyList(),
    val destinatario: Usuario? = null,
    val carregando: Boolean = false,
    val erro: String? = null
) : Parcelable