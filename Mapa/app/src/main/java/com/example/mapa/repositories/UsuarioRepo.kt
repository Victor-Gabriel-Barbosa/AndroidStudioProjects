package com.example.mapa.repositories

import com.example.mapa.models.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório de usuários
 */
interface UsuarioRepo {
    suspend fun save(usuario: Usuario): Result<Boolean>
    fun findAll(): Flow<List<Usuario>>
    fun findByUid(uid: String): Flow<List<Usuario>>
    suspend fun updateByUid(usuarioUid: String, usuario: Usuario): Result<Boolean>
    suspend fun deleteByUid(usuarioUid: String): Result<Boolean>
}