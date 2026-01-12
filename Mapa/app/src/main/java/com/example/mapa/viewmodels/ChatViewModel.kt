package com.example.mapa.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapa.models.Chat
import com.example.mapa.models.ChatUiState
import com.example.mapa.models.Mensagem
import com.example.mapa.models.Usuario
import com.example.mapa.repositories.AuthRepo
import com.example.mapa.repositories.ChatRepo
import com.example.mapa.repositories.UsuarioRepo
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
 * ViewModel para tela de chat
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val chatRepo: ChatRepo,
    authRepo: AuthRepo,
    private val usuarioRepo: UsuarioRepo
) : ViewModel() {
    // Uid do outro usuário na sala
    private val _destinatarioUid = MutableStateFlow<String?>(null)

    // Canal de mensagens
    private val _mensagens = Channel<String>(Channel.BUFFERED)
    val mensagens = _mensagens.receiveAsFlow()

    // Uid do usuário logado
    val remetenteUid: StateFlow<String?> = authRepo.usuarioState
        .map { it?.uid }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Estado do chat (mensagens e destinatario)
    val uiState: StateFlow<ChatUiState> = combine(
        _destinatarioUid.filterNotNull(),
        remetenteUid.filterNotNull()
    ) { destinatarioUid, meuUid ->
        Pair(destinatarioUid, meuUid)
    }.flatMapLatest { (destinatarioUid, meuUid) ->
        val salaId = gerarSalaId(meuUid, destinatarioUid)

        combine(
            carregarMsgsFlow(salaId),
            carregarDestinatarioFlow(destinatarioUid)
        ) { msgs, destinatario ->
            ChatUiState(msgs = msgs, destinatario = destinatario)
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChatUiState(carregando = true)
        )

    // Inicia o chat com o outro usuário
    fun iniciarChat(destinatarioUid: String) {
        _destinatarioUid.value = destinatarioUid
    }

    // Carrega as mensagens do chat
    private fun carregarMsgsFlow(salaId: String): Flow<List<Mensagem>> {
        return chatRepo.findById(salaId)
            .onEach { msgs -> marcarMsgsComoLidas(salaId, msgs) }
            .catch { e ->
                _mensagens.send("Erro ao carregar mensagens: ${e.message}")
                emit(emptyList())
            }
    }

    // Carrega o destinatario do chat
    private fun carregarDestinatarioFlow(uid: String): Flow<Usuario?> {
        return usuarioRepo.findByUid(uid)
            .map { it.firstOrNull() }
            .catch {
                _mensagens.send("Erro ao carregar destinatario: ${it.message}")
                emit(null)
            }
    }

    // Envia uma mensagem no chat
    fun enviarMsg(msg: Mensagem) {
        if (msg.texto.isBlank() && msg.imgUrls.isEmpty()) return

        val remetenteUid = this@ChatViewModel.remetenteUid.value ?: return
        val destinatario = _destinatarioUid.value ?: return
        val salaId = gerarSalaId(remetenteUid, destinatario)

        val novaMsg = msg.copy(
            remetenteUid = remetenteUid,
            destinatarioUid = destinatario,
            timestamp = System.currentTimeMillis(),
            lido = false
        )

        val chatResumo = Chat(
            ultimaMsg = msg,
            ultimoTimestamp = novaMsg.timestamp,
            participantes = listOf(remetenteUid, destinatario)
        )

        viewModelScope.launch {
            chatRepo.save(salaId, novaMsg, chatResumo)
                .onSuccess { _mensagens.send("Mensagem enviada!") }
                .onFailure { e -> _mensagens.send("Falha ao enviar: ${e.message}") }
        }
    }

    // Edita uma mensagem no chat
    fun editarMsg(msgId: String, novaMsg: Mensagem) {
        val meuUid = remetenteUid.value ?: return
        val destinatarioUid = _destinatarioUid.value ?: return
        val salaId = gerarSalaId(meuUid, destinatarioUid)

        viewModelScope.launch {
            chatRepo.updateMsgById(salaId, msgId, novaMsg)
                .onSuccess { _mensagens.send("Mensagem atualizada!") }
                .onFailure { e -> _mensagens.send("Falha ao atualizar: ${e.message}") }
        }
    }

    // Exclui uma mensagem do chat
    fun excluirMsg(msgId: String) {
        val meuUid = remetenteUid.value ?: return
        val destinatarioUid = _destinatarioUid.value ?: return
        val salaId = gerarSalaId(meuUid, destinatarioUid)

        viewModelScope.launch {
            chatRepo.deleteMsgById(salaId, msgId)
                .onSuccess { _mensagens.send("Mensagem excluída!") }
                .onFailure { e -> _mensagens.send("Falha ao excluir: ${e.message}") }
        }
    }

    // Marca as mensagens como lidas
    private fun marcarMsgsComoLidas(salaId: String, msgs: List<Mensagem>) {
        val destinatarioUid = _destinatarioUid.value ?: return
        val precisaAtualizar = msgs.any { it.remetenteUid == destinatarioUid && !it.lido }

        if (precisaAtualizar) {
            viewModelScope.launch {
                chatRepo.updateMsgsLidasById(salaId, destinatarioUid)
                    .onFailure { e -> _mensagens.send("Falha ao marcar como lida: ${e.message}") }
                    .onSuccess { _mensagens.send("Mensagens marcadas como lidas!") }
            }
        }
    }

    // Gera o id da sala a partir dos Uids dos usuários
    private fun gerarSalaId(usuario1: String, usuario2: String): String {
        return if (usuario1 < usuario2) "${usuario1}_${usuario2}" else "${usuario2}_${usuario1}"
    }
}