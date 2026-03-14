package com.example.mapa.ui.navigation

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
 * Define todas as rotas do app e suas propriedades.
 */
enum class AppRotas(
    val route: Routes,
    @get:StringRes val label: Int,
    val icon: ImageVector,
    val iconFill: ImageVector
) {
    HOME(Routes.Home, R.string.inicio, Icons.Outlined.Home, Icons.Default.Home),
    SAVED(Routes.Saved, R.string.salvos, Icons.Outlined.Bookmarks, Icons.Default.Bookmarks),
    CHAT(Routes.ChatRoutes, R.string.mensagens, Icons.AutoMirrored.Outlined.Message, Icons.AutoMirrored.Filled.Message),
    PROFILE(Routes.Profile, R.string.perfil, Icons.Outlined.AccountCircle, Icons.Default.AccountCircle)
}