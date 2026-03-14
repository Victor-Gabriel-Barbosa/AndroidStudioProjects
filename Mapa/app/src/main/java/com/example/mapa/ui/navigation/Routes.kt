package com.example.mapa.ui.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Define todas as rotas de navegação do app.
 */
sealed interface Routes : Parcelable {
    // Rotas de Autenticação / Inicialização
    @Parcelize data object Signin : Routes
    @Parcelize data object Signup : Routes
    @Parcelize data object LoginAnimation : Routes

    // Rotas Principais (Logado)
    @Parcelize data object Home : Routes
    @Parcelize data object Saved : Routes
    @Parcelize data object Profile : Routes
    @Parcelize data object ChatRoutes : Routes
    @Parcelize data object ChatList : Routes
    @Parcelize data class Chat(val uid: String, val localId: String) : Routes
}