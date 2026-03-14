package com.example.mapa.data.remote.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa um usuário no banco de dados remoto.
 */
@Parcelize
data class UserDTO (
    val uid: String = "",
    val name: String? = null,
    val email: String? = null,
    val photo: String? = null,
    val averageRating: Double = 0.0,
    val ratingCount: Int = 0,
    val reviewerUids: List<String> = emptyList(),
    val fcmToken: String = ""
) : Parcelable