package com.example.mapa.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um usuário
 */
@Parcelize
data class Usuario (
    val uid: String = "",
    val nome: String? = null,
    val email: String? = null,
    val foto: String? = null
) : Parcelable