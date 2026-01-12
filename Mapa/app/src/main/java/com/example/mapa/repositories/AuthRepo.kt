package com.example.mapa.repositories

import com.example.mapa.models.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório de autenticação
 */
interface AuthRepo {
    val usuarioState: Flow<Usuario?>
    val usuarioLogado: Flow<Boolean?>
    suspend fun signInWithEmail(email: String, senha: String): Result<Boolean>
    suspend fun signUpWithEmail(email: String, senha: String): Result<Boolean>
    suspend fun signInWithGoogle(credencial: Any): Result<Boolean>
    fun signOut()
}