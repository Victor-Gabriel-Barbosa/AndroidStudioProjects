package com.example.mapa.data.remote

import com.example.mapa.data.remote.dto.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório de usuários
 */
interface UsuarioRepository {
    suspend fun save(usuario: Usuario): Result<Boolean>
    fun findAll(): Flow<List<Usuario>>
    fun findByUid(uid: String): Flow<List<Usuario>>
    suspend fun updateByUid(uid: String, usuario: Usuario): Result<Boolean>
    suspend fun deleteByUid(uid: String): Result<Boolean>
}