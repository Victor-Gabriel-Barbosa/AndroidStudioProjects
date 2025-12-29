package com.example.refindu.repos

import com.example.refindu.models.User
import kotlinx.coroutines.flow.Flow

// Interface para repositório de autenticação
interface AuthRepo {
    val userState: Flow<User?>
    suspend fun signInWithEmail(email: String, pass: String): Result<Boolean>
    suspend fun signUpWithEmail(email: String, pass: String): Result<Boolean>
    suspend fun signInWithGoogle(credential: Any): Result<Boolean>
    fun signOut()
}