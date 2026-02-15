package com.example.mapa.models

import android.os.Parcelable
import com.example.mapa.data.remote.dto.Usuario
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado do chat
 */
@Parcelize
data class ChatState(
    val msgs: List<Mensagem> = emptyList(),
    val contato: Usuario? = null,
    val carregando: Boolean = false,
    val carregandoFoto: Boolean = false,
    val erro: String? = null
) : Parcelable