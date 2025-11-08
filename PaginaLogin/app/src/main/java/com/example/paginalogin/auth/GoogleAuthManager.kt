package com.example.paginalogin.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.paginalogin.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthManager(
    private val context: Context,
    private val auth: FirebaseAuth
) {
    object NonceGenerator {
        fun generate(): String {
            val ranNonce = UUID.randomUUID().toString()
            val bytes = ranNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    }

    suspend fun signIn(
        credentialManager: CredentialManager,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val nonce = NonceGenerator.generate()
            val webClientId = context.getString(R.string.default_web_client_id)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(nonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            handleCredentialResponse(result, onSuccess, onError)

        } catch (e: Exception) {
            onError("Erro ao fazer login com Google: ${e.localizedMessage}")
        }
    }

    private suspend fun handleCredentialResponse(
        result: GetCredentialResponse,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.Companion
                            .createFrom(credential.data)

                        val firebaseCredential = GoogleAuthProvider
                            .getCredential(googleIdTokenCredential.idToken, null)

                        val authResult = auth.signInWithCredential(firebaseCredential).await()
                        authResult.user?.let { onSuccess(it) }
                            ?: onError("Usuário não encontrado")

                    } catch (e: Exception) {
                        onError("Erro na autenticação: ${e.localizedMessage}")
                    }
                } else {
                    onError("Tipo de credencial não suportado")
                }
            }
            else -> {
                onError("Tipo de credencial inesperado")
            }
        }
    }
}