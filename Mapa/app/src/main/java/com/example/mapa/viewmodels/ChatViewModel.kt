package com.example.mapa.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.data.remote.dto.Chat
import com.example.mapa.models.ChatState
import com.example.mapa.models.Mensagem
import com.example.mapa.data.remote.dto.Usuario
import com.example.mapa.data.remote.AuthRepository
import com.example.mapa.data.remote.ChatRepository
import com.example.mapa.data.remote.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel para a tela de chat.
 *
 * @property chatRepository Repositório para operações relacionadas a chats.
 * @property authRepository Repositório para operações de autenticação.
 * @property usuarioRepository Repositório para operações relacionadas a usuários.
 * @property savedStateHandle Handle para o estado salvo da ViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val chatRepository: ChatRepository,
    authRepository: AuthRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {
    /**
     * O UID do destinatário (usuário selecionado na lista de chats).
     */
    private val _contatoUid = MutableStateFlow<String?>(null)
    private val _carregando = MutableStateFlow(false)

    /**
     * Canal para enviar mensagens de Snackbar para a UI.
     */
    private val _mensagens = Channel<String>(Channel.BUFFERED)
    val mensagens = _mensagens.receiveAsFlow()

    /**
     * O UID do autor (usuário logado).
     */
    val autorUid: StateFlow<String?> = authRepository.usuarioState
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Combinação de dados entre o autor e o contato para carregar as mensagens.
     */
    private val dadosChatFlow = combine(
        autorUid.filterNotNull(),
        _contatoUid.filterNotNull()
    ) { meuUid, contatoUid -> Pair(meuUid, contatoUid) }
        .flatMapLatest { (meuUid, contatoUid) ->
            val salaId = gerarSalaId(meuUid, contatoUid)
            combine(
                carregarMsgsFlow(salaId),
                carregarContatoFlow(contatoUid)
            ) { msgs, contato -> Pair(msgs, contato) }
        }

    /**
     * O estado da UI para a tela de chat.
     */
    val uiState: StateFlow<ChatState> = combine(
        dadosChatFlow,
        _carregando
    ) { (msgs, contato), carregando ->
        ChatState(
            msgs = msgs,
            contato = contato,
            carregando = carregando,
            carregandoFoto = contato?.foto == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatState(carregando = true, carregandoFoto = true)
    )

    fun inicializar(uid: String) {
        if (_contatoUid.value != uid) _contatoUid.value = uid
    }

    /**
     * Carrega as mensagens do chat.
     *
     * @param salaId O ID da sala de chat.
     * @return Um fluxo de mensagens do chat.
     */
    private fun carregarMsgsFlow(salaId: String): Flow<List<Mensagem>> {
        return chatRepository.findById(salaId)
            .onEach { msgs -> marcarMsgsComoLidas(salaId, msgs) }
            .catch { e ->
                Log.e("ChatViewModel", "carregarMsgsFlow: ${e.message}")
                _mensagens.send("Erro ao carregar mensagens: ${e.message}")
                emit(emptyList())
            }
    }

    private fun carregarContatoFlow(uid: String): Flow<Usuario?> {
        return usuarioRepository.findByUid(uid)
            .map { it.firstOrNull() }
            .catch {
                Log.e("ChatViewModel", "carregarContatoFlow: ${it.message}")
                _mensagens.send("Erro ao carregar contato: ${it.message}")
                emit(null)
            }
    }

    /**
     * Envia uma nova mensagem.
     *
     * @param msg A mensagem a ser enviada.
     */
    fun enviarMsg(msg: Mensagem) {
        if (msg.texto.isBlank() && msg.imgUrls.isEmpty()) return

        val autorUid = this@ChatViewModel.autorUid.value ?: return
        val contato = _contatoUid.value ?: return
        val salaId = gerarSalaId(autorUid, contato)

        val novaMsg = msg.copy(
            autorUid = autorUid,
            timestamp = System.currentTimeMillis(),
            lido = false
        )

        val chatResumo = Chat(
            ultimaMsg = novaMsg,
            ultimoTimestamp = novaMsg.timestamp,
            participantes = listOf(autorUid, contato)
        )

        viewModelScope.launch {
            _carregando.value = true
            chatRepository.save(salaId, novaMsg, chatResumo)
                .onFailure { e ->
                    Log.e("ChatViewModel", "enviarMsg: ${e.message}")
                    _mensagens.send("Falha ao enviar: ${e.message}")
                }
            _carregando.value = false
        }
    }

    /**
     * Edita uma mensagem existente.
     *
     * @param msgId O ID da mensagem a ser editada.
     * @param novaMsg A nova mensagem.
     */
    fun editarMsg(msgId: String, novaMsg: Mensagem) {
        val meuUid = autorUid.value ?: return
        val contatoUid = _contatoUid.value ?: return
        val salaId = gerarSalaId(meuUid, contatoUid)

        viewModelScope.launch {
            _carregando.value = true
            chatRepository.updateMsgById(salaId, msgId, novaMsg)
                .onFailure { e ->
                    Log.e("ChatViewModel", "editarMsg: ${e.message}")
                    _mensagens.send("Falha ao atualizar: ${e.message}")
                }
            _carregando.value = false
        }
    }

    /**
     * Exclui uma mensagem.
     *
     * @param msgId O ID da mensagem a ser excluída.
     */
    fun excluirMsg(msgId: String) {
        val meuUid = autorUid.value ?: return
        val contatoUid = _contatoUid.value ?: return
        val salaId = gerarSalaId(meuUid, contatoUid)

        viewModelScope.launch {
            _carregando.value = true
            chatRepository.deleteMsgById(salaId, msgId)
                .onFailure { e ->
                    Log.e("ChatViewModel", "excluirMsg: ${e.message}")
                    _mensagens.send("Falha ao excluir: ${e.message}")
                }
            _carregando.value = false
        }
    }

    /**
     * Marca as mensagens como lidas.
     *
     * @param salaId O ID da sala de chat.
     * @param msgs As mensagens a serem marcadas como lidas.
     */
    private fun marcarMsgsComoLidas(salaId: String, msgs: List<Mensagem>) {
        val contatoUid = _contatoUid.value ?: return
        val atualizar = msgs.any { it.autorUid == contatoUid && !it.lido }

        if (atualizar) {
            viewModelScope.launch {
                chatRepository.updateMsgsLidasById(salaId, contatoUid)
                    .onFailure { e ->
                        Log.e("ChatViewModel", "marcarMsgsComoLidas: ${e.message}")
                        _mensagens.send("Falha ao marcar como lida: ${e.message}")
                    }
            }
        }
    }

    /**
     * Gera um ID de sala com base nos UIDs dos participantes.
     *
     * @param usuario1 O UID do primeiro participante.
     * @param usuario2 O UID do segundo participante.
     * @return O ID da sala gerado.
     */
    private fun gerarSalaId(usuario1: String, usuario2: String): String {
        return if (usuario1 < usuario2) "${usuario1}_${usuario2}" else "${usuario2}_${usuario1}"
    }
}