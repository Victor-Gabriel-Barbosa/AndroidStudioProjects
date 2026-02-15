package com.example.mapa.models

import android.os.Parcelable
import com.example.mapa.data.remote.dto.Usuario
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado de um usuário
 */
@Parcelize
data class UsuarioState (
    val usuario: Usuario? = null,
    val logado: Boolean? = null,
    val carregandoFoto: Boolean = false,
    val carregandoNome: Boolean = false
) : Parcelable