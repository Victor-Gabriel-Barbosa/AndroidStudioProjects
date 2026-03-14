package com.example.mapa.model

/**
 * Classe que representa o estado de autenticação.
 */
sealed class LoginUiState {
    data object Stopped : LoginUiState()
    data object Loading : LoginUiState()
    data object Success : LoginUiState()
    data class Error(val msg: String) : LoginUiState()
}