package com.example.mapa.data.remote.datasource

import com.example.mapa.data.remote.dto.UserDTO
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório remoto de usuários.
 */
interface UserRemote {
    suspend fun save(user: UserDTO): Result<Boolean>
    fun getAll(): Flow<List<UserDTO>>
    fun getByUid(uid: String): Flow<UserDTO?>
    suspend fun updateByUid(uid: String, user: UserDTO): Result<Boolean>
    suspend fun deleteByUid(uid: String): Result<Boolean>
}