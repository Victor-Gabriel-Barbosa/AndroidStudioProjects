package com.example.mapa.repositories

import com.example.mapa.models.Local
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório de locais perdidos
 */
interface LocalRepo {
    suspend fun save(local: Local): Result<Boolean>
    fun findAll(): Flow<List<Local>>
    fun findByUid(uid: String): Flow<List<Local>>
    suspend fun updateById(localId: String, local: Local): Result<Boolean>
    suspend fun deleteById(localId: String): Result<Boolean>
}