package com.example.mapa.model

import android.os.Parcelable
import com.example.mapa.data.remote.dto.LocationDTO
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

/**
 * Estados possíveis do BottomSheet na tela de Home.
 */
@Parcelize
sealed interface SheetUiState : Parcelable {
    data object Hidden : SheetUiState

    data class Adding(
        val latLng: LatLng,
        val radius: Double = 50.0
    ) : SheetUiState

    data class Viewing(
        val location: LocationDTO
    ) : SheetUiState

    data class Editing(
        val location: LocationDTO,
        val radius: Double = location.radius
    ) : SheetUiState
}