package com.example.mapa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapa.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório local de locais perdidos.
 */
@Dao
interface LocationDao {
    @Query("SELECT * FROM location")
    fun getAll(): Flow<List<LocationEntity>>
    @Query("SELECT * FROM location WHERE uid = :uid")
    fun getByUid(uid: String): Flow<List<LocationEntity>>
    @Query("SELECT * FROM location WHERE id = :id")
    suspend fun getById(id: String): List<LocationEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)
    @Query("DELETE FROM location WHERE id = :id")
    suspend fun deleteById(id: String)
    @Query("DELETE FROM location")
    suspend fun clearAll()
}