package com.example.mapa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapa.data.local.entity.LocalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório local de locais perdidos.
 */
@Dao
interface LocalDao {
    @Query("SELECT * FROM local")
    fun getAll(): Flow<List<LocalEntity>>
    @Query("SELECT * FROM local WHERE uid = :uid")
    fun getByUid(uid: String): Flow<List<LocalEntity>>
    @Query("SELECT * FROM local WHERE id = :id")
    suspend fun getById(id: String): List<LocalEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(local: LocalEntity)
    @Query("UPDATE local SET entregue = 1 WHERE id = :id")
    suspend fun updateEntregueById(id: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locais: List<LocalEntity>)
    @Query("DELETE FROM local WHERE id = :id")
    suspend fun deleteById(id: String)
    @Query("DELETE FROM local")
    suspend fun clearAll()
}