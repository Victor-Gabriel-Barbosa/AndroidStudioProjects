package com.example.mapa.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.data.remote.datasource.AuthRemote
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.model.ChatItem
import com.example.mapa.model.ChatListUiState
import com.example.mapa.data.repository.ChatRepository
import com.example.mapa.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para a tela de lista de chats.
 *
 * @property authRemote Repositório para operações de autenticação.
 * @property chatRepo Repositório para operações relacionadas a chats.
 * @property userRepo Repositório para operações relacionadas a usuários.
 */
class ChatListViewModel(
    authRemote: AuthRemote,
    private val chatRepo: ChatRepository,
    private val userRepo: UserRepository
) : ViewModel() {
    /**
     * O UID do usuário logado.
     */
    private val authorUid: StateFlow<String?> = authRemote.user
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Lista base de itens de chat (chat + contato).
     *
     * Este [StateFlow] é a fonte de verdade para os dados exibidos na lista.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val chatItems: StateFlow<List<ChatItem>> = authorUid
        .filterNotNull()
        .flatMapLatest(::buildChatItems)
        .catch { e ->
            Log.e("ChatListViewModel", "chatItems: ${e.message}")
            emit(emptyList())
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * A quantidade de mensagens não lidas do usuário.
     *
     * Este [StateFlow] emite a quantidade de mensagens não lidas do usuário.
     * Se o usuário não estiver logado, emite 0.
     */
    val unreadCount: StateFlow<Int> = combine(authorUid, chatItems) { myUid, items ->
        val uid = myUid ?: return@combine 0
        items.count { item ->
            val unread = !(item.chat.lastMsg?.read ?: true)
            val nonOwner = item.chat.lastMsg?.uid != uid
            unread && nonOwner
        }
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    /**
     * O estado da UI para a tela de lista de chats.
     *
     * Este [StateFlow] emite o [ChatListUiState] que contém a lista de conversas do usuário,
     * o status de carregamento e quaisquer erros que possam ter ocorrido.
     */
    val uiState: StateFlow<ChatListUiState> = chatItems
        .map { chats ->
            ChatListUiState(chats = chats.sortedByDescending { it.chat.lastTimestamp }, loading = false, error = null)
        }
        .onStart {
            emit(ChatListUiState(loading = true))
        }
        .catch { e ->
            Log.e("ChatListViewModel", "uiState: ${e.message}")
            emit(ChatListUiState(loading = false, error = e.message ?: "Erro desconhecido"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatListUiState(loading = true)
        )

    /**
     * Monta a lista de [ChatItem] para o usuário logado.
     *
     * @param uid O UID do usuário logado.
     * @return Um [Flow] que emite a lista de [ChatItem].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildChatItems(uid: String): Flow<List<ChatItem>> {
        return chatRepo.getChats(uid).flatMapLatest { chats ->
            if (chats.isEmpty()) flowOf(emptyList())
            else {
                val flows = chats.map { chat -> chatItemFlow(chat, uid) }
                combine(flows) { it.toList() }
            }
        }
    }

    /**
     * Cria um [Flow] que emite um [ChatItem] a partir de um chat e do UID do usuário logado.
     *
     * @param chat O chat que será exibido.
     * @param authorUid O UID do usuário logado.
     * @return Um [Flow] que emite o [ChatItem] correspondente.
     */
    private fun chatItemFlow(chat: ChatDTO, authorUid: String): Flow<ChatItem> {
        val contactUid = chat.participants.find { it != authorUid } ?: ""

        return userRepo.getUser(contactUid)
            .onStart {
                if (contactUid.isNotEmpty()) {
                    viewModelScope.launch {
                        try {
                            userRepo.syncUser(contactUid)
                        } catch (e: Exception) {
                            Log.e("ChatListViewModel", "Erro sync contato: ${e.message}")
                        }
                    }
                }
            }
            .map { user ->
                ChatItem(
                    chat = chat,
                    contact = user
                )
            }
    }

    /**
     * "Exclui" múltiplas conversas do usuário (oculta).
     *
     * @param ids Um conjunto de IDs de salas de chat a serem excluídas.
     */
    fun deleteMsg(ids: Set<String>) {
        val uid = authorUid.value ?: return

        viewModelScope.launch {
            ids.forEach { id ->
                chatRepo.deleteChat(id, uid)
                    .onFailure { e -> Log.e("ChatListViewModel", "Erro ao ocultar: ${e.message}") }
            }
        }
    }
}