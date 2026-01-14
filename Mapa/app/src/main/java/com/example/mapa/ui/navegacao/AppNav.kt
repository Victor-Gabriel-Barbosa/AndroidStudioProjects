package com.example.mapa.ui.navegacao

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mapa.R

/**
 * Define todas as rotas do app e suas propriedades
 */
enum class AppNav(
    val rota: Rota,
    @get:StringRes val label: Int,
    val icon: ImageVector,
    val iconFill: ImageVector
) {
    HOME(Rota.Home, R.string.inicio, Icons.Outlined.Home, Icons.Default.Home),
    SALVOS(Rota.Salvos, R.string.salvos, Icons.Outlined.LocationOn, Icons.Default.LocationOn),
    CHAT(Rota.ChatGraph, R.string.mensagens, Icons.AutoMirrored.Outlined.Message, Icons.AutoMirrored.Filled.Message),
    PERFIL(Rota.Perfil, R.string.perfil, Icons.Outlined.AccountCircle, Icons.Default.AccountCircle)
}