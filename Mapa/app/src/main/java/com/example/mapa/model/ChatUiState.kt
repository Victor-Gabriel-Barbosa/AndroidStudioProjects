package com.example.mapa.model

import android.os.Parcelable
import com.example.mapa.data.remote.dto.MsgDTO
import com.example.mapa.data.remote.dto.UserDTO
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado do chat.
 */
@Parcelize
data class ChatUiState(
    val msgs: List<MsgDTO> = emptyList(),
    val contact: UserDTO? = null,
    val uid: String? = null,
    val loading: Boolean = false,
    val loadingPhoto: Boolean = false,
    val error: String? = null
) : Parcelable