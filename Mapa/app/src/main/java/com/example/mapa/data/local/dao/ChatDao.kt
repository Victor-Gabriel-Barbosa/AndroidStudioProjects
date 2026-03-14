package com.example.mapa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapa.data.local.entity.ChatEntity
import com.example.mapa.data.local.entity.MsgEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório local de conversas.
 */
@Dao
interface ChatDao {
    @Query("SELECT * FROM chat WHERE visibleTo LIKE '%' || :uid || '%' ORDER BY lastTimestamp DESC")
    fun getChatsByUid(uid: String): Flow<List<ChatEntity>>
    @Query("SELECT * FROM chat WHERE id = :id")
    suspend fun getChatById(id: String): ChatEntity?
    @Query("SELECT * FROM msg WHERE id = :id")
    suspend fun getMsgById(id: String): MsgEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)
    @Query("UPDATE chat SET visibleTo = :visibleTo WHERE id = :id")
    suspend fun updateVisibleToById(id: String, visibleTo: List<String>)
    @Query("SELECT * FROM msg WHERE chatId = :id ORDER BY timestamp ASC")
    fun getMsgsById(id: String): Flow<List<MsgEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMsg(msg: MsgEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMsgs(msg: List<MsgEntity>)
    @Query("UPDATE msg SET read = :read WHERE chatId = :id AND uid = :contactUid AND read != :read")
    suspend fun updateReadById(id: String, contactUid: String, read: Boolean)
    @Query("DELETE FROM msg WHERE id = :id")
    suspend fun deleteMsgById(id: String)
    @Query("DELETE FROM chat WHERE id = :id")
    suspend fun deleteChat(id: String)
}