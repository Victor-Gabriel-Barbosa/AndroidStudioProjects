package com.example.mapa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapa.data.local.entity.LocalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalDao {
    @Query("SELECT * FROM locais")
    fun getAll(): Flow<List<LocalEntity>>

    @Query("SELECT * FROM locais WHERE uid = :uid")
    fun getByUid(uid: String): Flow<List<LocalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(local: LocalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locais: List<LocalEntity>)

    @Query("DELETE FROM locais WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM locais")
    suspend fun clearAll()
}