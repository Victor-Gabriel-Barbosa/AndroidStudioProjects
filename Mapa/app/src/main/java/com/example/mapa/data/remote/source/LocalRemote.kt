package com.example.mapa.data.remote.source

import com.example.mapa.data.remote.dto.LocalDTO
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório remoto de locais perdidos.
 */
interface LocalRemote {
    suspend fun save(localDto: LocalDTO): Result<Boolean>
    fun findAll(): Flow<List<LocalDTO>>
    fun findByUid(uid: String): Flow<List<LocalDTO>>
    suspend fun updateById(localId: String, localDto: LocalDTO): Result<Boolean>
    suspend fun updateEntregueById(localId: String): Result<Boolean>
    suspend fun deleteById(localId: String): Result<Boolean>
}