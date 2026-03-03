package com.example.mapa.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.data.remote.datasource.AuthRemote
import com.example.mapa.model.ChatItem
import com.example.mapa.model.ChatListUiState
import com.example.mapa.data.repository.ChatRepository
import com.example.mapa.data.repository.UsuarioRepository
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
 * @property usuarioRepo Repositório para operações relacionadas a usuários.
 */
class ChatListViewModel(
    authRemote: AuthRemote,
    private val chatRepo: ChatRepository,
    private val usuarioRepo: UsuarioRepository
) : ViewModel() {
    /**
     * O UID do usuário logado.
     */
    private val autorUid: StateFlow<String?> = authRemote.usuario
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Lista base de itens de chat (chat + contato).
     *
     * Este [StateFlow] é a fonte de verdade para os dados exibidos na lista.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val chatItems: StateFlow<List<ChatItem>> = autorUid
        .filterNotNull()
        .flatMapLatest(::buildChatItems)
        .catch { e ->
            Log.e("ChatListViewModel", "chatItems: ${e.message}")
            emit(emptyList())
        }
        .stateIn(
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
    val qtdNaoLidas: StateFlow<Int> = combine(autorUid, chatItems) { meuUid, items ->
        val uid = meuUid ?: return@combine 0
        items.count { item ->
            val naoLido = !(item.chat.ultimaMsg?.lido ?: true)
            val msgDeOutro = item.chat.ultimaMsg?.autorUid != uid
            naoLido && msgDeOutro
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
            val chatsOrdenados = chats.sortedByDescending { it.chat.ultimoTimestamp }
            ChatListUiState(chats = chatsOrdenados, carregando = false, erro = null)
        }
        .onStart {
            emit(ChatListUiState(carregando = true))
        }
        .catch { e ->
            Log.e("ChatListViewModel", "uiState: ${e.message}")
            emit(ChatListUiState(carregando = false, erro = e.message ?: "Erro desconhecido"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatListUiState(carregando = true)
        )

    /**
     * Monta a lista de [ChatItem] para o usuário logado.
     *
     * @param uid O UID do usuário logado.
     * @return Um [Flow] que emite a lista de [ChatItem].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildChatItems(uid: String): Flow<List<ChatItem>> {
        return chatRepo.getChatsByUid(uid).flatMapLatest { listaDeChats ->
            if (listaDeChats.isEmpty()) flowOf(emptyList())
            else {
                val flows = listaDeChats.map { chat -> chatItemFlow(chat, uid) }
                combine(flows) { it.toList() }
            }
        }
    }

    /**
     * Cria um [Flow] que emite um [ChatItem] a partir de um chat e do UID do usuário logado.
     *
     * @param chat O chat que será exibido.
     * @param autorUid O UID do usuário logado.
     * @return Um [Flow] que emite o [ChatItem] correspondente.
     */
    private fun chatItemFlow(chat: com.example.mapa.data.remote.dto.ChatDTO, autorUid: String): Flow<ChatItem> {
        val contatoUid = chat.participantes.find { it != autorUid } ?: ""

        return usuarioRepo.getUsuario(contatoUid)
            .onStart {
                if (contatoUid.isNotEmpty()) {
                    viewModelScope.launch {
                        try {
                            usuarioRepo.syncContato(contatoUid)
                        } catch (e: Exception) {
                            Log.e("ChatListViewModel", "Erro sync contato: ${e.message}")
                        }
                    }
                }
            }
            .map { usuario ->
                ChatItem(
                    chat = chat,
                    contato = usuario
                )
            }
    }

    /**
     * "Exclui" múltiplas conversas do usuário (oculta).
     *
     * @param salaIds Um conjunto de IDs de salas de chat a serem excluídas.
     */
    fun excluirConversas(salaIds: Set<String>) {
        val uid = autorUid.value ?: return

        viewModelScope.launch {
            salaIds.forEach { salaId ->
                chatRepo.deleteChat(salaId, uid)
                    .onFailure { e ->
                        Log.e("ChatListViewModel", "Erro ao ocultar: ${e.message}")
                    }
            }
        }
    }
}