package com.example.mapa.data.remote.source

import com.example.mapa.data.remote.dto.UsuarioDTO
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório remoto de autenticação.
 */
interface AuthRemote {
    val usuarioState: Flow<UsuarioDTO?>
    val usuarioLogado: Flow<Boolean?>
    suspend fun signInWithEmail(email: String, senha: String): Result<Boolean>
    suspend fun signUpWithEmail(email: String, senha: String): Result<Boolean>
    suspend fun signInWithGoogle(credencial: Any): Result<Boolean>
    fun signOut()
}