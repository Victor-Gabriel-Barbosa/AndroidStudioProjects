package com.example.mapa.data.remote.datasource

import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.MensagemDTO
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório remoto de conversas.
 */
interface ChatRemote {
    suspend fun save(salaId: String, msg: MensagemDTO, chat: ChatDTO): Result<Boolean>
    fun findById(salaId: String): Flow<List<MensagemDTO>>
    fun findByUid(uid: String): Flow<List<ChatDTO>>
    suspend fun updateMsgsLidasById(salaId: String, uid: String): Result<Boolean>
    suspend fun deleteMsgById(salaId: String, msgId: String): Result<Boolean>
    suspend fun updateMsgById(salaId: String, msgId: String, msg: MensagemDTO): Result<Boolean>
    suspend fun ocultarChat(salaId: String, uid: String): Result<Boolean>
}