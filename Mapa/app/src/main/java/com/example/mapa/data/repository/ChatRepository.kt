package com.example.mapa.data.repository

import android.util.Log
import com.example.mapa.data.local.dao.ChatDao
import com.example.mapa.data.mapper.toDTO
import com.example.mapa.data.mapper.toEntity
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.MensagemDTO
import com.example.mapa.data.remote.datasource.ChatRemote
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
 * @property chatRemote A fonte de dados remota para conversas.
 * @property chatDao A fonte de dados local para conversas.
 */
class ChatRepository(
    private val chatRemote: ChatRemote,
    private val chatDao: ChatDao
) {
    /**
     * Encontra todas as conversas para um determinado UID de usuário.
     * Ele primeiro retorna os dados locais e depois sincroniza com a fonte remota.
     *
     * @param uid O identificador único do usuário.
     * @return Um fluxo emitindo uma lista de [ChatDTO].
     */
    fun getChatsByUid(uid: String): Flow<List<ChatDTO>> = channelFlow {
        launch {
            chatDao.getChatsByUid(uid)
                .map { it.map { e -> e.toDTO() } }
                .collectLatest { send(it) }
        }

        launch {
            chatRemote.findByUid(uid)
                .catch { Log.e("ChatRepo", "Erro sync chats: $it") }
                .collect { chats -> chatDao.insertChats(chats.map { it.toEntity() }) }
        }
    }

    /**
     * Encontra todas as mensagens para um determinado ID de sala de conversa.
     * Ele primeiro retorna os dados locais e depois sincroniza com a fonte remota.
     *
     * @param salaId O identificador único da sala de conversa.
     * @return Um fluxo emitindo uma lista de [MensagemDTO].
     */
    fun getMsgsBySalaId(salaId: String): Flow<List<MensagemDTO>> = channelFlow {
        launch {
            chatDao.getMsgsById(salaId)
                .map { it.map { e -> e.toDTO() } }
                .collectLatest { send(it) }
        }

        launch {
            chatRemote.findById(salaId)
                .catch { e -> Log.e("ChatRepo", "Erro sync mensagens da sala $salaId: $e") }
                .collect { remoteMsgs -> chatDao.insertMsgs(remoteMsgs.map { it.toEntity(salaId) }) }
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
    suspend fun insertMsg(msg: MensagemDTO, chat: ChatDTO): Result<Boolean> {
        chatDao.insertMsg(msg.toEntity(chat.salaId))
        val res = chatRemote.save(chat.salaId, msg, chat)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao salvar msg: ${res.exceptionOrNull()}")
            chatDao.deleteMsgById(msg.id)
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
    suspend fun updateMsg(salaId: String, msg: MensagemDTO): Result<Boolean> {
        val estadoAntigo = chatDao.getMsgById(msg.id)

        chatDao.insertMsg(msg.toEntity(salaId))
        val res = chatRemote.updateMsgById(salaId, msg.id, msg)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao atualizar msg: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) chatDao.insertMsg(estadoAntigo)
            else chatDao.deleteMsgById(msg.id)
        }

        return res
    }

    /**
     * Marca todas as mensagens em uma sala de conversa como lidas para um usuário específico.
     *
     * @param salaId O identificador único da sala de conversa.
     * @param uid O identificador único do usuário.
     * @param lido O estado de leitura da mensagem.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun updateMsgsLidas(salaId: String, uid: String, lido: Boolean): Result<Boolean> {
        val estadoAntigo = chatDao.getMsgsById(salaId).firstOrNull()

        chatDao.updateLidoById(salaId, uid, lido)
        val res = chatRemote.updateMsgsLidasById(salaId, uid)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao atualizar msgs: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) chatDao.insertMsgs(estadoAntigo)
            else chatDao.deleteChat(salaId)
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
    suspend fun deleteMsg(salaId: String, msgId: String): Result<Boolean> {
        val estadoAntigo = chatDao.getMsgById(msgId)

        chatDao.deleteMsgById(msgId)
        val res = chatRemote.deleteMsgById(salaId, msgId)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao deletar msg: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) chatDao.insertMsg(estadoAntigo)
            else chatDao.deleteMsgById(msgId)
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
    suspend fun deleteChat(salaId: String, uid: String): Result<Boolean> {
        val estadoAntigo = chatDao.getChatById(salaId)

        chatDao.deleteChat(salaId)
        val res = chatRemote.ocultarChat(salaId, uid)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao ocultar chat: ${res.exceptionOrNull()}")
            if (estadoAntigo != null) chatDao.insertChat(estadoAntigo)
            else chatDao.deleteChat(salaId)
        }

        return res
    }
}