package com.example.mapa.models

import android.os.Parcelable
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.UsuarioDTO
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um item na lista de chats.
 */
@Parcelize
data class ChatItem(
    val chatDto: ChatDTO,
    val contato: UsuarioDTO?
) : Parcelable