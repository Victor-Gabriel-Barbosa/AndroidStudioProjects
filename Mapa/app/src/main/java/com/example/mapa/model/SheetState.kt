package com.example.mapa.model

import android.os.Parcelable
import com.example.mapa.data.remote.dto.LocalDTO
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

/**
 * Estados possíveis do BottomSheet na tela de Home.
 */
@Parcelize
sealed interface SheetState : Parcelable {
    data object Hidden : SheetState

    data class Adicionando(
        val posicao: LatLng,
        val raio: Double = 50.0
    ) : SheetState

    data class Visualizando(
        val local: LocalDTO
    ) : SheetState

    data class Editando(
        val local: LocalDTO,
        val raio: Double = local.raio
    ) : SheetState
}