package com.example.mapa.data.remote

import com.example.mapa.data.remote.dto.Local
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório de locais perdidos
 */
interface LocalRepository {
    suspend fun save(local: Local): Result<Boolean>
    fun findAll(): Flow<List<Local>>
    fun findByUid(uid: String): Flow<List<Local>>
    suspend fun updateById(localId: String, local: Local): Result<Boolean>
    suspend fun deleteById(localId: String): Result<Boolean>
}