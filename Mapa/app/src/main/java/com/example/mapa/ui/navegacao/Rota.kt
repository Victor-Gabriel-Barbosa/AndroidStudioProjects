package com.example.mapa.ui.navegacao

import kotlinx.serialization.Serializable

/**
 * Define todas as rotas do app
 */
sealed interface Rota {
    @Serializable data object Login : Rota
    @Serializable data object Signup : Rota
    @Serializable data object LoginAnimacao : Rota
    @Serializable data object Home : Rota
    @Serializable data object Salvos : Rota
    @Serializable data object Perfil : Rota
    @Serializable data object ChatGraph : Rota
    @Serializable data object ChatList : Rota
    @Serializable
    data class ChatDetalhe(val uid: String) : Rota
}