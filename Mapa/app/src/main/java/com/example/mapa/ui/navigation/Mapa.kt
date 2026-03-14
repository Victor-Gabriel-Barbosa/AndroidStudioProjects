package com.example.mapa.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.model.LoginUiState
import com.example.mapa.ui.components.LoadingOverlay
import com.example.mapa.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable principal que decide qual tela exibir com base no estado de autenticação.
 *
 * @param authViewModel O ViewModel para gerenciar a autenticação.
 */
@Composable
fun Mapa(
    authViewModel: AuthViewModel = koinViewModel()
) {
    val context = LocalContext.current

    // Coleta os estados do ViewModel
    val loginUiState by authViewModel.loginUiState.collectAsStateWithLifecycle()
    val userUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Feedback visual (eventos) vindo do ViewModel
    LaunchedEffect(Unit) {
        authViewModel.channel.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Lógica de decisão de qual "Mundo" exibir
    when (userUiState.loggedIn) {
        // Exibe a tela de carregamento enquanto verifica o estado de autenticação
        null -> {
            LoadingOverlay()
        }

        // Exibe a tela principal se o usuário estiver logado
        true if loginUiState is LoginUiState.Stopped -> {
            MapaNav(
                userUiState = userUiState
            )
        }

        // Caso contrário, exibe a tela de autenticação
        else -> {
            AuthNav(
                onLogin = { authViewModel.resetState() }
            )
        }
    }
}