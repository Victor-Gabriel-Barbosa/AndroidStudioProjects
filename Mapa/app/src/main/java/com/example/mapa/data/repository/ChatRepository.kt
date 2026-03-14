package com.example.mapa.data.repository

import android.util.Log
import com.example.mapa.data.local.dao.ChatDao
import com.example.mapa.data.mapper.toDTO
import com.example.mapa.data.mapper.toEntity
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.MsgDTO
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
    fun getChats(uid: String): Flow<List<ChatDTO>> = channelFlow {
        launch {
            chatDao.getChatsByUid(uid)
                .map { it.map { e -> e.toDTO() } }
                .collectLatest { send(it) }
        }

        launch {
            chatRemote.getByUid(uid)
                .catch { Log.e("ChatRepo", "Erro sync chats: $it") }
                .collect { chats -> chatDao.insertChats(chats.map { it.toEntity() }) }
        }
    }

    /**
     * Encontra todas as mensagens para um determinado ID de sala de conversa.
     * Ele primeiro retorna os dados locais e depois sincroniza com a fonte remota.
     *
     * @param id O identificador único da sala de conversa.
     * @return Um fluxo emitindo uma lista de [MsgDTO].
     */
    fun getMsgs(id: String): Flow<List<MsgDTO>> = channelFlow {
        launch {
            chatDao.getMsgsById(id)
                .map { it.map { e -> e.toDTO() } }
                .collectLatest { send(it) }
        }

        launch {
            chatRemote.getById(id)
                .catch { e -> Log.e("ChatRepo", "Erro sync mensagens da sala $id: $e") }
                .collect { remoteMsgs -> chatDao.insertMsgs(remoteMsgs.map { it.toEntity(id) }) }
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
    suspend fun insertMsg(msg: MsgDTO, chat: ChatDTO): Result<Boolean> {
        val previousMsg = chatDao.getMsgById(msg.id)

        chatDao.insertMsg(msg.toEntity(chat.id))
        val res = chatRemote.save(chat.id, msg, chat)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao salvar msg: ${res.exceptionOrNull()}")
            if (previousMsg != null) chatDao.insertMsg(previousMsg)
            else chatDao.deleteMsgById(msg.id)
        }

        return res
    }

    /**
     * Atualiza uma mensagem pelo seu ID em uma sala de conversa.
     *
     * @param id O identificador único da sala de conversa.
     * @param msgId O identificador único da mensagem.
     * @param msg O conteúdo atualizado da mensagem.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun updateMsg(id: String, msg: MsgDTO): Result<Boolean> {
        val previousMsg = chatDao.getMsgById(msg.id)

        chatDao.insertMsg(msg.toEntity(id))
        val res = chatRemote.updateMsgById(id, msg.id, msg)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao atualizar msg: ${res.exceptionOrNull()}")
            if (previousMsg != null) chatDao.insertMsg(previousMsg)
            else chatDao.deleteMsgById(msg.id)
        }

        return res
    }

    /**
     * Marca todas as mensagens em uma sala de conversa como lidas para um usuário específico.
     *
     * @param id O identificador único da sala de conversa.
     * @param uid O identificador único do usuário.
     * @param read O estado de leitura da mensagem.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun updateMsgsRead(id: String, uid: String, read: Boolean): Result<Boolean> {
        val previousChat = chatDao.getMsgsById(id).firstOrNull()

        chatDao.updateReadById(id, uid, read)
        val res = chatRemote.updateMsgsReadById(id, uid)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao atualizar msgs: ${res.exceptionOrNull()}")
            if (previousChat != null) chatDao.insertMsgs(previousChat)
            else chatDao.deleteChat(id)
        }

        return res
    }

    /**
     * Exclui uma mensagem pelo seu ID de uma sala de conversa.
     *
     * @param id O identificador único da sala de conversa.
     * @param msgId O identificador único da mensagem.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun deleteMsg(id: String, msgId: String): Result<Boolean> {
        val previousMsg = chatDao.getMsgById(msgId)

        chatDao.deleteMsgById(msgId)
        val res = chatRemote.deleteMsgById(id, msgId)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao deletar msg: ${res.exceptionOrNull()}")
            if (previousMsg != null) chatDao.insertMsg(previousMsg)
            else chatDao.deleteMsgById(msgId)
        }

        return res
    }

    /**
     * Oculta uma conversa para um usuário específico.
     *
     * @param id O identificador único da sala de conversa.
     * @param uid O identificador único do usuário.
     * @return Um [Result] indicando sucesso ou falha.
     */
    suspend fun deleteChat(id: String, uid: String): Result<Boolean> {
        val previousChat = chatDao.getChatById(id)

        chatDao.deleteChat(id)
        val res = chatRemote.hideChat(id, uid)

        if (res.isFailure) {
            Log.e("ChatRepository", "Erro ao ocultar chat: ${res.exceptionOrNull()}")
            if (previousChat != null) chatDao.insertChat(previousChat)
            else chatDao.deleteChat(id)
        }

        return res
    }
}