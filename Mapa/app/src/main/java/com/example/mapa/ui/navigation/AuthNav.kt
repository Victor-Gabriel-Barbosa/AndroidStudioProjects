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
import androidx.navigation.compose.NavHost
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.example.mapa.R
import com.example.mapa.model.LoginUiState
import com.example.mapa.ui.components.LottieAnimation
import com.example.mapa.ui.screen.SignInScreen
import com.example.mapa.ui.screen.SignUpScreen
import com.example.mapa.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Lida com o fluxo de autenticação (login e cadastro).
 *
 * Utiliza um [NavHost] para gerenciar as transições entre as diferentes telas (`[SignInScreen]`, `[SignUpScreen]`).
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
        authViewModel.channel.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Inicia com a tela de Login
    val backStack = rememberSaveable { mutableStateListOf<Routes>(Routes.Signin) }

    // Gerencia o estado do login e exibe mensagens de erro ou sucesso
    LaunchedEffect(loginUiState) {
        when (loginUiState) {
            is LoginUiState.Error -> {
                Toast.makeText(context, (loginUiState as LoginUiState.Error).msg, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }

            is LoginUiState.Success -> {
                backStack.clear()
                backStack.add(Routes.LoginAnimation)
            }

            else -> {}
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(innerPadding)
        ) { route ->
            when(route) {
                Routes.Signin -> NavEntry(route) {
                    SignInScreen(
                        onLoginWithEmail = { email, senha -> authViewModel.loginWithEmail(email, senha) },
                        onLoginWithGoogle = { cred -> authViewModel.loginWithGoogle(cred) },
                        onNavSignup = {
                            if (backStack.lastOrNull() != Routes.Signup) backStack.add(Routes.Signup)
                        },
                        loginUiState = loginUiState
                    )
                }

                Routes.Signup -> NavEntry(route) {
                    SignUpScreen(
                        onSignup = { email, senha -> authViewModel.registerWithEmail(email, senha) },
                        onNavLogin = {
                            if (backStack.size > 1) backStack.removeLastOrNull()
                        },
                        loginUiState = loginUiState
                    )
                }

                Routes.LoginAnimation -> NavEntry(route) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            animation = R.raw.login_sucesso_animacao,
                            loop = false,
                            speed = 3f,
                            onFinish = { onLogin() },
                            modifier = Modifier.size(200.dp)
                        )
                    }
                }

                else -> error("Rota não encontrada: $route")
            }
        }
    }
}