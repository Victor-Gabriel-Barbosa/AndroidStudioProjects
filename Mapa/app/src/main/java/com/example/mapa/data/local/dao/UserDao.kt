package com.example.mapa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapa.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório local de usuários.
 */
@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE uid = :uid")
    fun getById(uid: String): Flow<UserEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)
    @Query("DELETE FROM user")
    suspend fun deleteAll()
    @Query("DELETE FROM user WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)
}