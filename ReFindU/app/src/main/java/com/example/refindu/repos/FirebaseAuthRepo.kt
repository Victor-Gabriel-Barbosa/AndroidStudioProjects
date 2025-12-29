package com.example.refindu.repos

import com.example.refindu.models.User
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

// Implementação do repositório de autenticação delegando para o Firebase Auth
class FirebaseAuthRepo(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepo {

    // Função auxiliar para converter (Mapeamento)
    private fun FirebaseUser.toAppUser(): User {
        return User(
            uid = this.uid,
            email = this.email,
            displayName = this.displayName,
            photoUrl = this.photoUrl?.toString()
        )
    }

    // Flow central que escuta as mudanças do Firebase em tempo real
    override val userState: Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toAppUser()) }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    // Autenticação via provedor de E-mail/Senha com encapsulamento de Result
    override suspend fun signInWithEmail(email: String, pass: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, pass).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Registro de nova conta via provedor de E-mail/Senha
    override suspend fun signUpWithEmail(email: String, pass: String): Result<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, pass).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Autenticação via credenciais externas (OAuth/Google)
    override suspend fun signInWithGoogle(credential: Any): Result<Boolean> {
        return try {
            if (credential is AuthCredential) {
                auth.signInWithCredential(credential).await()
                Result.success(true)
            } else {
                Result.failure(IllegalArgumentException("Credencial inválida para Firebase"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Encerra a sessão atual e limpa o estado local
    override fun signOut() {
        auth.signOut()
    }
}