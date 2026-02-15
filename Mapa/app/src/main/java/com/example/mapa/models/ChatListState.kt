package com.example.mapa.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado da lista de chats
 */
@Parcelize
data class ChatListState(
    val chats: List<ChatItem> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null
) : Parcelable