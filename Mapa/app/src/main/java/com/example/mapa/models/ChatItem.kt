package com.example.mapa.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um item na lista de chats
 */
@Parcelize
data class ChatItem(
    val chat: Chat,
    val destinatario: Usuario?
) : Parcelable