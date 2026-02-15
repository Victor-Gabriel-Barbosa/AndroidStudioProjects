package com.example.mapa.data.remote

import android.util.Log
import com.example.mapa.data.remote.dto.Usuario
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * Implementação do [AuthRepository] que utiliza o Firebase Authentication para gerenciar
 * a autenticação de usuários.
 *
 * @property auth Instância do [FirebaseAuth] utilizada para todas as operações de autenticação.
 */
class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
): AuthRepository {

    /**
     * Um [Flow] que emite o estado atual do usuário ([Usuario]).
     * Emite um objeto [Usuario] se o usuário estiver logado, ou `null` caso contrário.
     * O Flow é atualizado em tempo real sempre que o estado de autenticação muda.
     */
    override val usuarioState: Flow<Usuario?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toUsuario()) }
        auth.addAuthStateListener(authStateListener)
        awaitClose { auth.removeAuthStateListener(authStateListener) }
    }

    /**
     * Um [Flow] que emite `true` se houver um usuário logado e `false` caso contrário.
     * Derivado do [usuarioState].
     */
    override val usuarioLogado: Flow<Boolean?> = usuarioState.map { it != null }

    /**
     * Tenta autenticar um usuário com e-mail e senha.
     *
     * @param email O e-mail do usuário.
     * @param senha A senha do usuário.
     * @return [Result.success] com `true` se o login for bem-sucedido, [Result.failure] com a exceção em caso de erro.
     */
    override suspend fun signInWithEmail(email: String, senha: String): Result<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, senha).await()
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
     * @param senha A senha para a nova conta.
     * @return [Result.success] com `true` se o cadastro for bem-sucedido, [Result.failure] com a exceção em caso de erro.
     */
    override suspend fun signUpWithEmail(email: String, senha: String): Result<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, senha).await()
            Result.success(true)
        } catch (e: Exception) {
            Log.e("AuthFirebaseRepo", "signUpWithEmail: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Tenta autenticar um usuário utilizando uma credencial do Google (obtida através do One Tap).
     *
     * @param credencial A [AuthCredential] do Google.
     * @return [Result.success] com `true` se o login for bem-sucedido, [Result.failure] com a exceção em caso de erro.
     */
    override suspend fun signInWithGoogle(credencial: Any): Result<Boolean> {
        return try {
            if (credencial is AuthCredential) {
                auth.signInWithCredential(credencial).await()
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
     * Converte um objeto [FirebaseUser] do Firebase para o modelo de domínio [Usuario].
     */
    private fun FirebaseUser.toUsuario(): Usuario {
        return Usuario(
            uid = this.uid,
            email = this.email,
            nome = this.displayName,
            foto = this.photoUrl?.toString()
        )
    }
}