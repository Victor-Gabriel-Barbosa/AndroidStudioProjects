package com.example.mapa.data.remote.datasource

import com.example.mapa.data.remote.dto.UserDTO
import kotlinx.coroutines.flow.Flow

/**
 * Interface que define os métodos do repositório remoto de autenticação.
 */
interface AuthRemote {
    val user: Flow<UserDTO?>
    val loggedInUser: Flow<Boolean?>
    suspend fun signInWithEmail(email: String, password: String): Result<Boolean>
    suspend fun signUpWithEmail(email: String, password: String): Result<Boolean>
    suspend fun signInWithGoogle(credential: Any): Result<Boolean>
    suspend fun getFcmToken(): String?
    fun signOut()
}