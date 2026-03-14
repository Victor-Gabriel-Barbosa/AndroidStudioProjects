package com.example.mapa.data.remote.datasource

import com.example.mapa.data.remote.dto.LocationDTO
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório remoto de locais perdidos.
 */
interface LocationRemote {
    suspend fun save(location: LocationDTO): Result<Boolean>
    fun getAll(): Flow<List<LocationDTO>>
    fun getByUid(uid: String): Flow<List<LocationDTO>>
    suspend fun updateById(id: String, location: LocationDTO): Result<Boolean>
    suspend fun deleteById(id: String): Result<Boolean>
}