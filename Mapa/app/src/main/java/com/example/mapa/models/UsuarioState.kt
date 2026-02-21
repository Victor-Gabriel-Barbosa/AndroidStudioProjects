package com.example.mapa.models

import android.os.Parcelable
import com.example.mapa.data.remote.dto.UsuarioDTO
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado de um usuário.
 */
@Parcelize
data class UsuarioState (
    val usuarioDto: UsuarioDTO? = null,
    val logado: Boolean? = null,
    val carregandoFoto: Boolean = false,
    val carregandoNome: Boolean = false
) : Parcelable