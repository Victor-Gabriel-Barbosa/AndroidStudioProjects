package com.example.refindu.repos

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

// Implementação do repositório de autenticação delegando para o Firebase Auth
class FirebaseAuthRepo(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepo {

    // Verificação de estado da sessão (usuário ativo)
    override val isUserSignedIn: Boolean get() = auth.currentUser != null

    // Acesso seguro ao ID do usuário autenticado (nullable)
    override val userUid: String? get() = auth.currentUser?.uid

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
    override suspend fun signInWithGoogle(credential: AuthCredential): Result<Boolean> {
        return try {
            auth.signInWithCredential(credential).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Encerra a sessão atual e limpa o estado local
    override fun signOut() {
        auth.signOut()
    }
}