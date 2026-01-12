package com.example.mapa.repositories

import com.example.mapa.models.Usuario
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Implementação do repositório de autenticação com FirebaseAuth
 */
class AuthFirebaseRepo(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
): AuthRepo {

    // Flow para monitorar o estado do usuário logado
    override val usuarioState: Flow<Usuario?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toAppUser()) }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    // Flow para verificar se o usuário está logado
    override val usuarioLogado: Flow<Boolean?> = usuarioState.map { it != null }

    // Autenticação com email
    override suspend fun signInWithEmail(email: String, senha: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, senha).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cadastro com email
    override suspend fun signUpWithEmail(email: String, senha: String): Result<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, senha).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Autenticação com Google
    override suspend fun signInWithGoogle(credencial: Any): Result<Boolean> {
        return try {
            if (credencial is AuthCredential) {
                auth.signInWithCredential(credencial).await()
                Result.success(true)
            } else Result.failure(IllegalArgumentException("Credencial inválida para Firebase"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Desloga o usuário
    override fun signOut() {
        auth.signOut()
    }

    // Converte um FirebaseUser para Usuario
    private fun FirebaseUser.toAppUser(): Usuario {
        return Usuario(
            uid = this.uid,
            email = this.email,
            nome = this.displayName,
            foto = this.photoUrl?.toString()
        )
    }
}