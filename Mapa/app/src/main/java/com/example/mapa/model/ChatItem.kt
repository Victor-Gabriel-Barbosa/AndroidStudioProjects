package com.example.mapa.model

import android.os.Parcelable
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.UsuarioDTO
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um item na lista de chats.
 */
@Parcelize
data class ChatItem(
    val chat: ChatDTO,
    val contato: UsuarioDTO?
) : Parcelable