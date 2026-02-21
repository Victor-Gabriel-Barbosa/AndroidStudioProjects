package com.example.mapa.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.data.remote.source.AuthRemote
import com.example.mapa.models.ChatItem
import com.example.mapa.models.ChatListState
import com.example.mapa.data.repository.ChatRepository
import com.example.mapa.data.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para a tela de lista de chats.
 *
 * @property chatRepo Repositório para operações relacionadas a chats.
 * @property authRemote Repositório para operações de autenticação.
 * @property usuarioRepo Repositório para operações relacionadas a usuários.
 */
class ChatListViewModel(
    private val chatRepo: ChatRepository,
    authRemote: AuthRemote,
    private val usuarioRepo: UsuarioRepository
) : ViewModel() {
    /**
     * O UID do usuário logado.
     */
    private val autorUid: StateFlow<String?> = authRemote.usuarioDTOState
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * A quantidade de mensagens não lidas do usuário.
     *
     * Este [StateFlow] emite a quantidade de mensagens não lidas do usuário.
     * Se o usuário não estiver logado, emite 0.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val qtdNaoLidas: StateFlow<Int> = autorUid
        .filterNotNull()
        .flatMapLatest { meuUid ->
            chatRepo.carregarChats(meuUid).map { chats ->
                chats.count { chat ->
                    val naoLido = !(chat.ultimaMsg?.lido ?: true)
                    val msgDeOutro = chat.ultimaMsg?.autorUid != meuUid
                    naoLido && msgDeOutro
                }
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
     * Este [StateFlow] emite o [ChatListState] que contém a lista de conversas do usuário,
     * o status de carregamento e quaisquer erros que possam ter ocorrido.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ChatListState> = autorUid
        .filterNotNull()
        .flatMapLatest { autorUid ->
            chatRepo.carregarChats(autorUid).flatMapLatest { listaDeChats ->
                if (listaDeChats.isEmpty()) flowOf(emptyList())
                else {
                    val flows = listaDeChats.map { chat ->
                        val contatoUid = chat.participantes.find { it != autorUid } ?: ""

                        usuarioRepo.carregarUsuario(contatoUid)
                            .onEach { usuario ->
                                if (usuario == null && contatoUid.isNotEmpty()) {
                                    viewModelScope.launch {
                                        try {
                                            usuarioRepo.syncContato(contatoUid)
                                        } catch (e: Exception) {
                                            Log.e("ChatListVM", "Erro sync contato: ${e.message}")
                                        }
                                    }
                                }
                            }
                            .map { usuario ->
                                ChatItem(
                                    chatDto = chat,
                                    contato = usuario
                                )
                            }
                    }
                    combine(flows) { it.toList() }
                }
            }
        }
        .map { chats ->
            val chatsOrdenados = chats.sortedByDescending { it.chatDto.ultimoTimestamp }
            ChatListState(chats = chatsOrdenados, carregando = false, erro = null)
        }
        .onStart {
            emit(ChatListState(carregando = true))
        }
        .catch { e ->
            Log.e("ChatListViewModel", "uiState: ${e.message}")
            emit(ChatListState(carregando = false, erro = e.message ?: "Erro desconhecido"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatListState(carregando = true)
        )

    /**
     * "Exclui" múltiplas conversas do usuário (oculta).
     *
     * @param salaIds Um conjunto de IDs de salas de chat a serem excluídas.
     */
    fun excluirConversas(salaIds: Set<String>) {
        val uid = autorUid.value ?: return

        viewModelScope.launch {
            salaIds.forEach { salaId ->
                chatRepo.deletarChat(salaId, uid)
                    .onFailure { e ->
                        Log.e("ChatListViewModel", "Erro ao ocultar: ${e.message}")
                    }
            }
        }
    }
}