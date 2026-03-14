package com.example.mapa.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado da lista de chats.
 */
@Parcelize
data class ChatListUiState(
    val chats: List<ChatItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
) : Parcelable