package com.example.mapa.model

/**
 * Classe que representa o estado de autenticação.
 */
sealed class LoginUiState {
    data object Parado : LoginUiState()
    data object Carregando : LoginUiState()
    data object Sucesso : LoginUiState()
    data class Erro(val msg: String) : LoginUiState()
}