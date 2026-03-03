package com.example.mapa.data.remote.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Classe que representa um local no banco de dados remoto.
 */
@Parcelize
data class LocalDTO (
    val id: String = "",
    val uid: String = "",
    val nome: String = "",
    val tipo: String = "",
    val descricao: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val raio: Double = 0.0,
    val data: Date? = null,
    val imgUrls: List<String> = emptyList()
) : Parcelable