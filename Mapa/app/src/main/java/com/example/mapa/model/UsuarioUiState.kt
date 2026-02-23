package com.example.mapa.model

import android.os.Parcelable
import com.example.mapa.data.remote.dto.UsuarioDTO
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado de um usuário.
 */
@Parcelize
data class UsuarioUiState (
    val usuario: UsuarioDTO? = null,
    val logado: Boolean? = null,
    val carregandoFoto: Boolean = false,
    val carregandoNome: Boolean = false
) : Parcelable