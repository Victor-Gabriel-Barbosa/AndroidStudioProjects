package com.example.mapa.data.remote.datasource

import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.MsgDTO
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório remoto de conversas.
 */
interface ChatRemote {
    suspend fun save(id: String, msg: MsgDTO, chat: ChatDTO): Result<Boolean>
    fun getById(id: String): Flow<List<MsgDTO>>
    fun getByUid(uid: String): Flow<List<ChatDTO>>
    suspend fun updateMsgsReadById(id: String, uid: String): Result<Boolean>
    suspend fun deleteMsgById(id: String, msgId: String): Result<Boolean>
    suspend fun updateMsgById(id: String, msgId: String, msg: MsgDTO): Result<Boolean>
    suspend fun hideChat(id: String, uid: String): Result<Boolean>
}