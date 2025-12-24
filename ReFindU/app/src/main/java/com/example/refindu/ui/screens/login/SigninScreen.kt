package com.example.refindu.ui.screens.login

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.refindu.R
import com.example.refindu.viewmodels.LoginState // Importe o LoginState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun SigninScreen(
    state: LoginState,
    onLoginClick: (String, String) -> Unit,
    onGoogleLoginClick: (AuthCredential) -> Unit,
    onNavigateToSignup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.fazer_login), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.e_mail)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is LoginState.Loading // Desabilita enquanto carrega
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.senha)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is LoginState.Loading
        )

        // Exibe erro de validação local ou erro vindo do ViewModel (Firebase)
        if (validationError != null) Text(text = validationError!!, color = MaterialTheme.colorScheme.error)
        else if (state is LoginState.Error) Text(text = state.message, color = MaterialTheme.colorScheme.error)

        Spacer(modifier = Modifier.height(24.dp))

        // Botão de Login ou Loading
        if (state is LoginState.Loading) CircularProgressIndicator()
        else {
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        validationError = null
                        onLoginClick(email, password)
                    } else validationError = context.getString(R.string.preencha_todos_os_campos)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.entrar))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botão Google
        OutlinedButton(
            enabled = state !is LoginState.Loading,
            onClick = {
                scope.launch {
                    val credential = signInWithGoogle(context, credentialManager)
                    if (credential != null) onGoogleLoginClick(credential)
                    else Log.d("Login", "Login Google cancelado")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = stringResource(R.string.logo_do_google),
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = stringResource(R.string.entrar_com_google))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToSignup,
            enabled = state !is LoginState.Loading
        ) {
            Text(stringResource(R.string.nao_tem_conta_cadastre_se))
        }
    }
}

// Função para login com Google
suspend fun signInWithGoogle(context: Context, credentialManager: CredentialManager): AuthCredential? {
    try {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(request = request, context = context)
        val credential = result.credential

        if (credential is GoogleIdTokenCredential) return GoogleAuthProvider.getCredential(credential.idToken, null)

        return null
    } catch (e: GetCredentialException) {
        Log.e("LoginGoogle", "Error: ${e.message}")
        return null
    }
}