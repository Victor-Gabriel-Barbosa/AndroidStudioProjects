package com.example.mapa.ui.navigation

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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.mapa.R
import com.example.mapa.model.LoginUiState
import com.example.mapa.ui.components.AnimacaoLottie
import com.example.mapa.ui.screens.TelaSignIn
import com.example.mapa.ui.screens.TelaSignUp
import com.example.mapa.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Lida com o fluxo de autenticação (login e cadastro).
 *
 * Este composable gerencia a navegação entre as telas de login e cadastro,
 * observa o estado do login e exibe mensagens de erro ou sucesso.
 *
 * @param authViewModel O ViewModel que gerencia o estado de autenticação.
 * @param loginState O estado atual do processo de login.
 * @param onLoginConcluido Callback a ser invocado quando o login for concluído com sucesso.
 */
@Composable
fun AuthNav(
    onLoginConcluido: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Observáveis do ViewModel
    val loginState by authViewModel.loginUiState.collectAsStateWithLifecycle()

    // Feedback visual (eventos) vindo do ViewModel
    LaunchedEffect(Unit) {
        authViewModel.canal.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Inicia com a tela de Login
    val backStack = rememberSaveable { mutableStateListOf<Rotas>(Rotas.Login) }

    // Gerencia o botão de voltar do Android
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    // Gerencia o estado do login e exibe mensagens de erro ou sucesso
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginUiState.Erro -> {
                Toast.makeText(context, (loginState as LoginUiState.Erro).mensagem, Toast.LENGTH_LONG).show()
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
                Rotas.Login -> NavEntry(rota) {
                    TelaSignIn(
                        onLogin = { email, senha -> authViewModel.login(email, senha) },
                        onGoogleLogin = { cred -> authViewModel.loginWithGoogle(cred) },
                        onNavSignup = {
                            if (backStack.lastOrNull() != Rotas.Signup) backStack.add(Rotas.Signup)
                        },
                        loginUiState = loginState
                    )
                }

                Rotas.Signup -> NavEntry(rota) {
                    TelaSignUp(
                        onSignup = { email, senha -> authViewModel.cadastrar(email, senha) },
                        onNavegarParaLogin = {
                            if (backStack.size > 1) backStack.removeLastOrNull()
                        },
                        loginUiState = loginState
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
                            onConcluir = { onLoginConcluido() },
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }

                else -> error("Rota não encontrada: $rota")
            }
        }
    }
}