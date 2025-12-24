package com.example.refindu.repos

import com.google.firebase.auth.AuthCredential

// Interface para repositório de autenticação
interface AuthRepo {
    val isUserSignedIn: Boolean
    val userUid: String?
    suspend fun signInWithEmail(email: String, pass: String): Result<Boolean>
    suspend fun signUpWithEmail(email: String, pass: String): Result<Boolean>
    suspend fun signInWithGoogle(credential: AuthCredential): Result<Boolean>
    fun signOut()
}