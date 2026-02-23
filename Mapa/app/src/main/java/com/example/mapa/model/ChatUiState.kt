package com.example.mapa.model

import android.os.Parcelable
import com.example.mapa.data.remote.dto.MensagemDTO
import com.example.mapa.data.remote.dto.UsuarioDTO
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado do chat.
 */
@Parcelize
data class ChatUiState(
    val msgs: List<MensagemDTO> = emptyList(),
    val contato: UsuarioDTO? = null,
    val carregando: Boolean = false,
    val carregandoFoto: Boolean = false,
    val erro: String? = null
) : Parcelable