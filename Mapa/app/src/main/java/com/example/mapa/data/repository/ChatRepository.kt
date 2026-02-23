package com.example.mapa.data.repository

import android.util.Log
import com.example.mapa.data.local.dao.ChatDao
import com.example.mapa.data.mapper.toDomain
import com.example.mapa.data.mapper.toEntity
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.MensagemDTO
import com.example.mapa.data.remote.source.ChatRemote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Repositório para lidar com operações de dados relacionadas a conversas.
 * Ele abstrai as fontes de dados (remota e local) e fornece uma API limpa para a UI.
 *
 * @property remote A fonte de dados remota para conversas.
 * @property local A fonte de dados local para conversas.
 */
class ChatRepository(
    private val remote: ChatRemote,
    private val local: ChatDao
) {
    /**
     * Encontra todas as conversas para um determinado UID de usuário.
     * Ele primeiro retorna os dados locais e depois sincroniza com a fonte remota.
     *
     * @param uid O identificador único do usuário.
     * @return Um fluxo emitindo uma lista de [ChatDTO].
     */
    fun carregarChats(uid: String): Flow<List<ChatDTO>> = channelFlow {
        launch {
            local.getChatsByUid(uid)
                .map { it.map { e -> e.toDomain() } }
                .collectLatest { send(it) }
        }

        launch {
            remote.findByUid(uid)
                .catch { Log.e("ChatRepo", "Erro sync chats: $it") }
                .collect { chats -> local.insertChats(chats.map { it.toEntity() }) }
        }
    }

    /**
     * Encontra todas as mensagens para um determinado ID de sala de conversa.
     * Ele primeiro retorna os dados locais e depois sincroniza com a fonte remota.
     *
     * @param salaId O identificador único da sala de conversa.
     * @return Um fluxo emitindo uma lista de [MensagemDTO].
     */
    fun carregarMsgs(salaId: String): Flow<List<MensagemDTO>> = channelFlow {
        launch {
            local.getMsgsById(salaId)
                .map { it.map { e -> e.toDomain() } }
                .collectLatest { send(it) }
        }

        launch {
            remote.findById(salaId)
                .catch { e -> Log.e("ChatRepo", "Erro sync mensagens da sala $salaId: $e") }
                .collect { remoteMsgs -> local.insertMsgs(remoteMsgs.map { it.toEntity(salaId) }) }
        }
    }

    /**
     * Salva uma nova mensagem em uma sala de conversa.
     *
     * @param salaId O identificador único da sala de conversa.
     * @param msg A mensagem a ser salva.
     * @param chat O objeto de transferência de dados da conversa.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun salvarMsg(salaId: String, msg: MensagemDTO, chat: ChatDTO): Result<Boolean> {
        local.insertMsg(msg.toEntity(salaId))
        val res = remote.save(salaId, msg, chat)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao salvar msg: ${res.exceptionOrNull()}")
            local.deleteMsgById(msg.id)
        }

        return res
    }

    /**
     * Atualiza uma mensagem pelo seu ID em uma sala de conversa.
     *
     * @param salaId O identificador único da sala de conversa.
     * @param msgId O identificador único da mensagem.
     * @param msg O conteúdo atualizado da mensagem.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun atualizarMsgs(salaId: String, msgId: String, msg: MensagemDTO): Result<Boolean> {
        val estadoAntigo = local.getMsgById(msgId)

        local.insertMsg(msg.toEntity(salaId))
        val res = remote.updateMsgById(salaId, msgId, msg)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao atualizar msg: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) local.insertMsg(estadoAntigo)
            else local.deleteMsgById(msgId)
        }

        return res
    }

    /**
     * Marca todas as mensagens em uma sala de conversa como lidas para um usuário específico.
     *
     * @param salaId O identificador único da sala de conversa.
     * @param uid O identificador único do usuário.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun atualizarMsgsLidas(salaId: String, uid: String, lido: Boolean): Result<Boolean> {
        val estadoAntigo = local.getMsgsById(salaId).firstOrNull()

        local.updateLidoById(salaId, uid, lido)
        val res = remote.updateMsgsLidasById(salaId, uid)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao atualizar msgs: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) local.insertMsgs(estadoAntigo)
            else local.deleteChat(salaId)
        }

        return res
    }

    /**
     * Exclui uma mensagem pelo seu ID de uma sala de conversa.
     *
     * @param salaId O identificador único da sala de conversa.
     * @param msgId O identificador único da mensagem.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun deletarMsgs(salaId: String, msgId: String): Result<Boolean> {
        val estadoAntigo = local.getMsgById(msgId)

        local.deleteMsgById(msgId)
        val res = remote.deleteMsgById(salaId, msgId)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao deletar msg: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) local.insertMsg(estadoAntigo)
            else local.deleteMsgById(msgId)
        }

        return res
    }

    /**
     * Oculta uma conversa para um usuário específico.
     *
     * @param salaId O identificador único da sala de conversa.
     * @param uid O identificador único do usuário.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun deletarChat(salaId: String, uid: String): Result<Boolean> {
        val estadoAntigo = local.getChatById(salaId)

        local.deleteChat(salaId)
        val res = remote.ocultarChat(salaId, uid)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao ocultar chat: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) local.insertChat(estadoAntigo)
            else local.deleteChat(salaId)
        }

        return res
    }
}