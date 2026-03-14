package com.example.mapa.model

import android.os.Parcelable
import com.example.mapa.data.remote.dto.LocationDTO
import kotlinx.parcelize.Parcelize

/**
 * Classe que representa o estado de locais
 */
@Parcelize
data class LocationUiState(
    val locations: List<LocationDTO> = emptyList(),
    val locationsUser: List<LocationDTO> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
) : Parcelable