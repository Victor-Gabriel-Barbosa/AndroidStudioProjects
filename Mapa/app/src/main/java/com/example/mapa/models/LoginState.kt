package com.example.mapa.models

/**
 * Classe que representa o estado de autenticação
 */
sealed class LoginState {
    data object Parado : LoginState()
    data object Carregando : LoginState()
    data object Sucesso : LoginState()
    data class Erro(val mensagem: String) : LoginState()
}