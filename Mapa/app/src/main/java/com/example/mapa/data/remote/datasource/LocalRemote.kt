package com.example.mapa.data.remote.datasource

import com.example.mapa.data.remote.dto.LocalDTO
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório remoto de locais perdidos.
 */
interface LocalRemote {
    suspend fun save(local: LocalDTO): Result<Boolean>
    fun findAll(): Flow<List<LocalDTO>>
    fun findByUid(uid: String): Flow<List<LocalDTO>>
    suspend fun updateById(localId: String, local: LocalDTO): Result<Boolean>
    suspend fun deleteById(localId: String): Result<Boolean>
}