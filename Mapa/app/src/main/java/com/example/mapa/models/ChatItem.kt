package com.example.mapa.models

import android.os.Parcelable
import com.example.mapa.data.remote.dto.Chat
import com.example.mapa.data.remote.dto.Usuario
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um item na lista de chats
 */
@Parcelize
data class ChatItem(
    val chat: Chat,
    val contato: Usuario?
) : Parcelable