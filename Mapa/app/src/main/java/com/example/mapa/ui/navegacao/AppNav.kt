package com.example.mapa.ui.navegacao

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mapa.model.LoginUiState
import com.example.mapa.ui.components.FundoCarregando
import com.example.mapa.viewmodels.AuthViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Composable principal que decide qual tela exibir com base no estado de autenticação.
 *
 * @param authViewModel O ViewModel para gerenciar a autenticação.
 */
@Composable
fun AppNav(
    authViewModel: AuthViewModel = koinViewModel()
) {
    // Coleta os estados do ViewModel
    val loginUiState by authViewModel.loginUiState.collectAsStateWithLifecycle()
    val usuarioUiState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Lógica de decisão de qual "Mundo" exibir
    when (usuarioUiState.logado) {
        // Exibe a tela de carregamento enquanto verifica o estado de autenticação
        null -> {
            FundoCarregando()
        }

        // Exibe a tela principal se o usuário estiver logado
        true if loginUiState is LoginUiState.Parado -> {
            MapaNav(
                usuarioUiState = usuarioUiState
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