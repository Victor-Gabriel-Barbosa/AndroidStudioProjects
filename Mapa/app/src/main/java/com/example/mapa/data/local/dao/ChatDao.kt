package com.example.mapa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapa.data.local.entity.ChatEntity
import com.example.mapa.data.local.entity.MensagemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório local de conversas.
 */
@Dao
interface ChatDao {
    @Query("SELECT * FROM chat WHERE visivelPara LIKE '%' || :uid || '%' ORDER BY ultimoTimestamp DESC")
    fun getChatsByUid(uid: String): Flow<List<ChatEntity>>
    @Query("SELECT * FROM chat WHERE salaId = :salaId")
    suspend fun getChatById(salaId: String): ChatEntity?
    @Query("SELECT * FROM mensagem WHERE id = :id")
    suspend fun getMsgById(id: String): MensagemEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)
    @Query("UPDATE chat SET visivelPara = :novaLista WHERE salaId = :salaId")
    suspend fun updateVisivelParaById(salaId: String, novaLista: List<String>)
    @Query("SELECT * FROM mensagem WHERE salaId = :salaId ORDER BY timestamp ASC")
    fun getMsgsById(salaId: String): Flow<List<MensagemEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMsg(mensagem: MensagemEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMsgs(mensagens: List<MensagemEntity>)
    @Query("UPDATE mensagem SET lido = :lido WHERE salaId = :salaId AND autorUid = :contatoUid AND lido != :lido")
    suspend fun updateLidoById(salaId: String, contatoUid: String, lido: Boolean)
    @Query("DELETE FROM mensagem WHERE id = :id")
    suspend fun deleteMsgById(id: String)
    @Query("DELETE FROM chat WHERE salaId = :salaId")
    suspend fun deleteChat(salaId: String)
}