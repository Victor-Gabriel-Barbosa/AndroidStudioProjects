package com.example.mapa.ui.navegacao

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mapa.R

/**
 * Define todas as rotas do app e suas propriedades
 */
enum class AppNav(
    val rota: Rotas,
    @get:StringRes val label: Int,
    val icon: ImageVector,
    val iconFill: ImageVector
) {
    HOME(Rotas.Home, R.string.inicio, Icons.Outlined.Home, Icons.Default.Home),
    SALVOS(Rotas.Salvos, R.string.salvos, Icons.Outlined.Bookmarks, Icons.Default.Bookmarks),
    CHAT(Rotas.ChatGraph, R.string.mensagens, Icons.AutoMirrored.Outlined.Message, Icons.AutoMirrored.Filled.Message),
    PERFIL(Rotas.Perfil, R.string.perfil, Icons.Outlined.AccountCircle, Icons.Default.AccountCircle)
}