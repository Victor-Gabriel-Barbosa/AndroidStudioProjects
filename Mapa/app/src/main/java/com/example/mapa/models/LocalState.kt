package com.example.mapa.models

import android.os.Parcelable
import com.example.mapa.data.remote.dto.LocalDTO
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado de locais
 */
@Parcelize
data class LocalState(
    val locais: List<LocalDTO> = emptyList(),
    val locaisUsuario: List<LocalDTO> = emptyList(),
    val carregando: Boolean = false,
    val erro: String? = null
) : Parcelable