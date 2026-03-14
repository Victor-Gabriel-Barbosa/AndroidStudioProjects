package com.example.mapa.data.remote.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Classe que representa um local no banco de dados remoto.
 */
@Parcelize
data class LocationDTO (
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val type: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Double = 0.0,
    val date: Date? = null,
    val imgUrls: List<String> = emptyList()
) : Parcelable