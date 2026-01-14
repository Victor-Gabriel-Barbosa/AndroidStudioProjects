package com.example.mapa.ui.navegacao

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mapa.R
import com.example.mapa.models.LoginState
import com.example.mapa.ui.componentes.Animacao
import com.example.mapa.ui.telas.TelaSignIn
import com.example.mapa.ui.telas.TelaSignUp
import com.example.mapa.viewmodels.AuthViewModel

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
fun AuthFlow(
    authViewModel: AuthViewModel,
    loginState: LoginState,
    onLoginConcluido: () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginState.Erro -> {
                Toast.makeText(context, loginState.mensagem, Toast.LENGTH_LONG).show()
                authViewModel.resetState()
            }
            is LoginState.Sucesso -> {
                navController.navigate(Rota.LoginAnimacao) {
                    popUpTo<Rota.Login> { inclusive = true }
                }
            }
            else -> {}
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Rota.Login,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Rota.Login> {
                TelaSignIn(
                    onLoginClick = { email, senha -> authViewModel.login(email, senha) },
                    onGoogleLoginClick = { cred -> authViewModel.loginWithGoogle(cred) },
                    onNavegarParaSignup = { navController.navigate(Rota.Signup) },
                    loginState = loginState
                )
            }

            composable<Rota.Signup> {
                TelaSignUp(
                    onSignupClick = { email, senha -> authViewModel.cadastrar(email, senha) },
                    onNavegarParaLogin = { navController.popBackStack() },
                    loginState = loginState
                )
            }

            composable<Rota.LoginAnimacao> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Animacao(
                        animacao = R.raw.login_sucesso_animacao,
                        loop = false,
                        velocidade = 2f,
                        onConcluir = { onLoginConcluido() },
                        modifier = Modifier.size(200.dp)
                    )
                }
            }
        }
    }
}