package com.example.mapa.data.remote.datasource

import com.example.mapa.data.remote.dto.UsuarioDTO
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório remoto de usuários.
 */
interface UsuarioRemote {
    suspend fun save(usuario: UsuarioDTO): Result<Boolean>
    fun findAll(): Flow<List<UsuarioDTO>>
    fun findByUid(uid: String): Flow<UsuarioDTO?>
    suspend fun updateByUid(uid: String, usuario: UsuarioDTO): Result<Boolean>
    suspend fun deleteByUid(uid: String): Result<Boolean>
}