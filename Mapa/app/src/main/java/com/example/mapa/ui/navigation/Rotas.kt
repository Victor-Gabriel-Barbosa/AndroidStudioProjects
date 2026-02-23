package com.example.mapa.ui.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Define todas as rotas de navegação do app.
 */
sealed interface Rotas : Parcelable {
    // Rotas de Autenticação / Inicialização
    @Parcelize data object Splash : Rotas
    @Parcelize data object Login : Rotas
    @Parcelize data object Signup : Rotas
    @Parcelize data object LoginAnimacao : Rotas

    // Rotas Principais (Logado)
    @Parcelize data object Home : Rotas
    @Parcelize data object Salvos : Rotas
    @Parcelize data object Perfil : Rotas
    @Parcelize data object ChatGraph : Rotas
    @Parcelize data object ChatList : Rotas
    @Parcelize data class ChatDetalhe(val uid: String, val localId: String) : Rotas
}