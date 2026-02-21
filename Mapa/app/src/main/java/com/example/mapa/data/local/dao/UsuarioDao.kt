package com.example.mapa.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mapa.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório local de usuários.
 */
@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuario WHERE id = :id")
    fun getById(id: String): Flow<UsuarioEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioEntity)
    @Query("DELETE FROM usuario")
    suspend fun deleteAll()
    @Query("DELETE FROM usuario WHERE id = :id")
    suspend fun deleteById(id: String)
}