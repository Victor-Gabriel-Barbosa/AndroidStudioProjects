package com.example.mapa.data.remote

import com.example.mapa.data.remote.dto.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório de autenticação
 */
interface AuthRepository {
    val usuarioState: Flow<Usuario?>
    val usuarioLogado: Flow<Boolean?>
    suspend fun signInWithEmail(email: String, senha: String): Result<Boolean>
    suspend fun signUpWithEmail(email: String, senha: String): Result<Boolean>
    suspend fun signInWithGoogle(credencial: Any): Result<Boolean>
    fun signOut()
}