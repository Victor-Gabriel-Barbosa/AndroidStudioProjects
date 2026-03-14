package com.example.mapa.data.remote.firebase

import android.util.Log
import com.example.mapa.data.remote.dto.UserDTO
import com.example.mapa.data.remote.datasource.AuthRemote
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Implementação do [AuthRemote] que utiliza o Firebase Authentication para gerenciar
 * a autenticação de usuários.
 *
 * @property auth Instância do [FirebaseAuth] utilizada para todas as operações de autenticação.
 */
class AuthFirebase(
    private val auth: FirebaseAuth
): AuthRemote {
    /**
     * Um [Flow] que emite o estado atual do usuário ([UserDTO]).
     * Emite um objeto [UserDTO] se o usuário estiver logado, ou `null` caso contrário.
     * O Flow é atualizado em tempo real sempre que o estado de autenticação muda.
     */
    override val user: Flow<UserDTO?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toDTO()) }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    /**
     * Um [Flow] que emite `true` se houver um usuário logado e `false` caso contrário.
     * Derivado do [user].
     */
    override val loggedInUser: Flow<Boolean?> = user.map { it != null }

    /**
     * Tenta autenticar um usuário com e-mail e senha.
     *
     * @param email O e-mail do usuário.
     * @param password A senha do usuário.
     * @return [Result.success] com `true` se o login for bem-sucedido, [Result.failure] com a exceção em caso de erro.
     */
    override suspend fun signInWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthFirebaseRepo", "signInWithEmail: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Cria uma nova conta de usuário com e-mail e senha.
     *
     * @param email O e-mail para a nova conta.
     * @param password A senha para a nova conta.
     * @return [Result.success] com `true` se o cadastro for bem-sucedido, [Result.failure] com a exceção em caso de erro.
     */
    override suspend fun signUpWithEmail(email: String, password: String): Result<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthFirebaseRepo", "signUpWithEmail: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Tenta autenticar um usuário utilizando uma credencial do Google (obtida através do One Tap).
     *
     * @param credential A [com.google.firebase.auth.AuthCredential] do Google.
     * @return [Result.success] com `true` se o login for bem-sucedido, [Result.failure] com a exceção em caso de erro.
     */
    override suspend fun signInWithGoogle(credential: Any): Result<Boolean> {
        return try {
            if (credential is AuthCredential) {
                auth.signInWithCredential(credential).await()
                Result.success(true)
            } else Result.failure(IllegalArgumentException("Credencial inválida para Firebase"))
        } catch (e: Exception) {
            Log.e("AuthFirebaseRepo", "signInWithGoogle: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Desconecta o usuário atualmente autenticado.
     */
    override fun signOut() {
        auth.signOut()
    }

    /**
     * Obtém o token FCM do dispositivo.
     *
     * @return O token FCM ou `null` em caso de erro.
     */
    override suspend fun getFcmToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Converte um objeto [com.google.firebase.auth.FirebaseUser] do Firebase para o modelo de domínio [UserDTO].
     */
    private fun FirebaseUser.toDTO(): UserDTO {
        return UserDTO(
            uid = this.uid,
            email = this.email,
            name = this.displayName,
            photo = this.photoUrl?.toString()
        )
    }
}