package com.example.mapa.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.MensagemDTO
import com.example.mapa.data.remote.dto.UsuarioDTO
import com.example.mapa.data.remote.datasource.AuthRemote
import com.example.mapa.data.repository.ChatRepository
import com.example.mapa.data.repository.UsuarioRepository
import com.example.mapa.model.ChatUiState
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
import java.util.UUID

/**
 * ViewModel para a tela de chat.
 *
 * @property authRemote Repositório para operações de autenticação.
 * @property chatRepo Repositório para operações relacionadas a chats.
 * @property usuarioRepo Repositório para operações relacionadas a usuários.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    authRemote: AuthRemote,
    private val chatRepo: ChatRepository,
    private val usuarioRepo: UsuarioRepository,
) : ViewModel() {
    /**
     * O UID do destinatário (usuário selecionado na lista de chats).
     */
    private val _contatoUid = MutableStateFlow<String?>(null)

    /**
     * O ID do local selecionado na lista de chats.
     */
    private val _localId = MutableStateFlow<String?>(null)

    /**
     * O estado de carregamento da tela.
     */
    private val _carregando = MutableStateFlow(false)

    /**
     * Canal para enviar mensagens de eventos para a UI.
     */
    private val _canal = Channel<String>(Channel.BUFFERED)
    val canal = _canal.receiveAsFlow()

    /**
     * O UID do autor (usuário logado).
     */
    val autorUid: StateFlow<String?> = authRemote.usuario
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
                carregarContatoFlow(contatoUid),
            ) { msgs, contato -> Triple(msgs, contato, meuUid) }
        }

    /**
     * O estado da UI para a tela de chat.
     */
    val uiState: StateFlow<ChatUiState> = combine(
        dadosChatFlow,
        _carregando
    ) { (msgs, contato, autorUid), carregando ->
        ChatUiState(
            msgs = msgs,
            contato = contato,
            autorUid = autorUid,
            carregando = carregando,
            carregandoFoto = contato?.foto == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState(carregando = true, carregandoFoto = true)
    )

    fun inicializar(uid: String, localId: String) {
        if (_contatoUid.value != uid) _contatoUid.value = uid
        if (_localId.value != localId) _localId.value = localId
    }

    /**
     * Carrega as mensagens do chat.
     *
     * @param salaId O ID da sala de chat.
     * @return Um fluxo de mensagens do chat.
     */
    private fun carregarMsgsFlow(salaId: String): Flow<List<MensagemDTO>> {
        return chatRepo.getMsgsBySalaId(salaId)
            .onEach { msgs -> marcarMsgsComoLidas(salaId, msgs) }
            .catch { e ->
                Log.e("ChatViewModel", "carregarMsgsFlow: ${e.message}")
                _canal.send("Erro ao carregar mensagens: ${e.message}")
                emit(emptyList())
            }
    }

    /**
     * Carrega as informações do contato.
     *
     * @param uid O UID do contato.
     * @return Um [Flow] de informações do contato.
     */
    private fun carregarContatoFlow(uid: String): Flow<UsuarioDTO?> {
        return usuarioRepo.getUsuario(uid)
            .catch {
                Log.e("ChatViewModel", "carregarContatoFlow: ${it.message}")
                emit(null)
            }
    }

    /**
     * Envia uma nova mensagem.
     *
     * @param msg A mensagem a ser enviada.
     */
    fun enviarMsg(msg: MensagemDTO) {
        if (msg.texto.isBlank() && msg.imgUrls.isEmpty()) return

        val autorUid = this@ChatViewModel.autorUid.value ?: return
        val contato = _contatoUid.value ?: return
        val salaId = gerarSalaId(autorUid, contato)
        val localId = _localId.value ?: return

        val novaMsg = msg.copy(
            id = UUID.randomUUID().toString(),
            autorUid = autorUid,
            timestamp = System.currentTimeMillis(),
            lido = false
        )

        val chatResumo = ChatDTO(
            salaId = salaId,
            ultimaMsg = novaMsg,
            ultimoTimestamp = novaMsg.timestamp,
            participantes = listOf(autorUid, contato),
            visivelPara = listOf(autorUid, contato),
            localId = localId
        )

        viewModelScope.launch {
            _carregando.value = true
            chatRepo.insertMsg(novaMsg, chatResumo)
                .onFailure { e ->
                    Log.e("ChatViewModel", "enviarMsg: ${e.message}")
                    _canal.send("Falha ao enviar: ${e.message}")
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
    fun editarMsg(novaMsg: MensagemDTO) {
        val meuUid = autorUid.value ?: return
        val contatoUid = _contatoUid.value ?: return
        val salaId = gerarSalaId(meuUid, contatoUid)

        viewModelScope.launch {
            _carregando.value = true
            chatRepo.updateMsg(salaId, novaMsg)
                .onFailure { e ->
                    Log.e("ChatViewModel", "editarMsg: ${e.message}")
                    _canal.send("Falha ao atualizar: ${e.message}")
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
            chatRepo.deleteMsg(salaId, msgId)
                .onFailure { e ->
                    Log.e("ChatViewModel", "excluirMsg: ${e.message}")
                    _canal.send("Falha ao excluir: ${e.message}")
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
    private fun marcarMsgsComoLidas(salaId: String, msgs: List<MensagemDTO>) {
        val contatoUid = _contatoUid.value ?: return
        val atualizar = msgs.any { it.autorUid == contatoUid && !it.lido }

        if (atualizar) {
            viewModelScope.launch {
                chatRepo.updateMsgsLidas(salaId, contatoUid, true)
                    .onFailure { e ->
                        Log.e("ChatViewModel", "marcarMsgsComoLidas: ${e.message}")
                        _canal.send("Falha ao marcar como lida: ${e.message}")
                    }
            }
        }
    }

    /**
     * Avalia o usuário.
     *
     * @param nota A nota do usuário.
     */
    fun avaliarUsuario(nota: Double) {
        val meuUid = autorUid.value ?: return
        val contato = uiState.value.contato ?: return

        viewModelScope.launch {
            usuarioRepo.updateUsuarioNota(contato, meuUid, nota)
                .onFailure { e ->
                    Log.e("ChatViewModel", "avaliarUsuario: ${e.message}")
                    _canal.send("Falha ao avaliar: ${e.message}")
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