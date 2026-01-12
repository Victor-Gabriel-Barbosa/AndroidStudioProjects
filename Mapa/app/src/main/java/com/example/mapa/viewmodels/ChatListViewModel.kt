package com.example.mapa.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.models.ChatItem
import com.example.mapa.models.ChatListUiState
import com.example.mapa.repositories.AuthRepo
import com.example.mapa.repositories.ChatRepo
import com.example.mapa.repositories.UsuarioRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel para tela de lista de conversas
 */
class ChatListViewModel(
    private val chatRepo: ChatRepo,
    authRepo: AuthRepo,
    private val usuarioRepo: UsuarioRepo
) : ViewModel() {

    // Uid do usuário logado
    private val usuarioUid: StateFlow<String?> = authRepo.usuarioState
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Estado da lista de chats
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ChatListUiState> = usuarioUid
        .filterNotNull()
        .flatMapLatest { remetenteUid ->
            chatRepo.findByUid(remetenteUid).flatMapLatest { listaDeChats ->
                if (listaDeChats.isEmpty()) flowOf(emptyList())
                else {
                    combine(listaDeChats.map { chat ->
                        // Busca o destinatario (quem não é o usuário logado)
                        val destinatarioUid = chat.participantes.find { it != remetenteUid } ?: ""
                        usuarioRepo.findByUid(destinatarioUid)
                            .map { usuarios ->
                                ChatItem(
                                    chat = chat,
                                    destinatario = usuarios.firstOrNull()
                                )
                            }
                    }) { it.toList() }
                }
            }
        }
        .map { chats ->
            ChatListUiState(chats = chats, carregando = false, erro = null)
        }
        .onStart {
            emit(ChatListUiState(carregando = true))
        }
        .catch { e ->
            e.printStackTrace()
            emit(ChatListUiState(carregando = false, erro = e.message ?: "Erro desconhecido"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatListUiState(carregando = true)
        )
}