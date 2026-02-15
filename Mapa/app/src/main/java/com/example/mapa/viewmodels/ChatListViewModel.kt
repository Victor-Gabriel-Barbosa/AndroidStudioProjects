package com.example.mapa.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.models.ChatItem
import com.example.mapa.models.ChatListState
import com.example.mapa.data.remote.AuthRepository
import com.example.mapa.data.remote.ChatRepository
import com.example.mapa.data.remote.UsuarioRepository
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
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para a tela de lista de chats.
 *
 * @property chatRepository Repositório para operações relacionadas a chats.
 * @property authRepository Repositório para operações de autenticação.
 * @property usuarioRepository Repositório para operações relacionadas a usuários.
 */
class ChatListViewModel(
    private val chatRepository: ChatRepository,
    authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    /**
     * O UID do usuário logado.
     */
    private val autorUid: StateFlow<String?> = authRepository.usuarioState
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
            chatRepository.findByUid(meuUid).map { chats ->
                // Conta apenas mensagens não lidas enviadas por outra pessoa
                chats.count { chat ->
                    !(chat.ultimaMsg?.lido ?: false) && chat.ultimaMsg?.autorUid != meuUid
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
            chatRepository.findByUid(autorUid).flatMapLatest { listaDeChats ->
                if (listaDeChats.isEmpty()) flowOf(emptyList())
                else {
                    combine(listaDeChats.map { chat ->
                        // Busca o contato (quem não é o usuário logado)
                        val contatoUid = chat.participantes.find { it != autorUid } ?: ""

                        usuarioRepository.findByUid(contatoUid)
                            .map { usuarios ->
                                ChatItem(
                                    chat = chat,
                                    contato = usuarios.firstOrNull()
                                )
                            }
                    }) { it.toList() }
                }
            }
        }
        .map { chats ->
            ChatListState(chats = chats, carregando = false, erro = null)
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
     * "Exclui" um chat do usuário (oculta).
     *
     * @param salaId O ID da sala de chat a ser excluída.
     */
    fun excluirConversa(salaId: String) {
        val uid = autorUid.value ?: return

        viewModelScope.launch {
            chatRepository.ocultarChat(salaId, uid)
                .onFailure { e ->
                    Log.e("ChatListViewModel", "Erro ao ocultar: ${e.message}")
                }
        }
    }
}