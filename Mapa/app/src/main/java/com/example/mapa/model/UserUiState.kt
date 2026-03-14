package com.example.mapa.model

import android.os.Parcelable
import com.example.mapa.data.remote.dto.UserDTO
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado de um usuário.
 */
@Parcelize
data class UserUiState (
    val user: UserDTO? = null,
    val loggedIn: Boolean? = null,
    val loadingPhoto: Boolean = false,
    val loadingName: Boolean = false
) : Parcelable