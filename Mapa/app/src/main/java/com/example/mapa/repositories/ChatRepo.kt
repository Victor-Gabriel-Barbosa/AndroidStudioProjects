package com.example.mapa.repositories

import com.example.mapa.models.Chat
import com.example.mapa.models.Mensagem
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório de conversas
 */
interface ChatRepo {
    suspend fun save(salaId: String, msg: Mensagem, chat: Chat): Result<Boolean>
    fun findById(salaId: String): Flow<List<Mensagem>>
    fun findByUid(uid: String): Flow<List<Chat>>
    suspend fun updateMsgsLidasById(salaId: String, uid: String): Result<Boolean>
    suspend fun deleteMsgById(salaId: String, msgId: String): Result<Boolean>
    suspend fun updateMsgById(salaId: String, msgId: String, msg: Mensagem): Result<Boolean>
}