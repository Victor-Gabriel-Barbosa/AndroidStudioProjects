package com.example.mapa.ui.navegacao

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.mapa.R
import com.example.mapa.model.LoginUiState
import com.example.mapa.ui.components.AnimacaoLottie
import com.example.mapa.ui.telas.TelaSignIn
import com.example.mapa.ui.telas.TelaSignUp
import com.example.mapa.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Lida com o fluxo de autenticação (login e cadastro).
 *
 * Utiliza um [NavHost] para gerenciar as transições entre as diferentes telas (`[TelaSignIn]`, `[TelaSignUp]`).
 *
 * @param onLogin Callback a ser invocado quando o login for concluído com sucesso.
 * @param authViewModel O ViewModel que gerencia o estado de autenticação.
 */
@Composable
fun AuthNav(
    onLogin: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Observáveis do ViewModel
    val loginUiState by authViewModel.loginUiState.collectAsStateWithLifecycle()

    // Feedback visual (eventos) vindo do ViewModel
    LaunchedEffect(Unit) {
        authViewModel.canal.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Inicia com a tela de Login
    val backStack = rememberSaveable { mutableStateListOf<Rotas>(Rotas.Signin) }

    // Gerencia o botão de voltar do Android
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    // Gerencia o estado do login e exibe mensagens de erro ou sucesso
    LaunchedEffect(loginUiState) {
        when (loginUiState) {
            is LoginUiState.Erro -> {
                Toast.makeText(context, (loginUiState as LoginUiState.Erro).msg, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }

            is LoginUiState.Sucesso -> {
                backStack.clear()
                backStack.add(Rotas.LoginAnimacao)
            }

            else -> {}
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.padding(innerPadding)
        ) { rota ->
            when(rota) {
                Rotas.Signin -> NavEntry(rota) {
                    TelaSignIn(
                        onSignin = { email, senha -> authViewModel.login(email, senha) },
                        onGoogleSignin = { cred -> authViewModel.loginWithGoogle(cred) },
                        onNavSignup = {
                            if (backStack.lastOrNull() != Rotas.Signup) backStack.add(Rotas.Signup)
                        },
                        loginUiState = loginUiState
                    )
                }

                Rotas.Signup -> NavEntry(rota) {
                    TelaSignUp(
                        onSignup = { email, senha -> authViewModel.cadastrar(email, senha) },
                        onNavLogin = {
                            if (backStack.size > 1) backStack.removeLastOrNull()
                        },
                        loginUiState = loginUiState
                    )
                }

                Rotas.LoginAnimacao -> NavEntry(rota) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimacaoLottie(
                            animacao = R.raw.login_sucesso_animacao,
                            loop = false,
                            velocidade = 3f,
                            onConcluir = { onLogin() },
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }

                else -> error("Rota não encontrada: $rota")
            }
        }
    }
}