package com.example.mapa.data.remote.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um usuário no banco de dados remoto.
 */
@Parcelize
data class UsuarioDTO (
    val uid: String = "",
    val nome: String? = null,
    val email: String? = null,
    val foto: String? = null,
    val notaMedia: Double = 0.0,
    val notaQtd: Int = 0,
    val avaliadores: List<String> = emptyList(),
    val fcmToken: String = ""
) : Parcelable