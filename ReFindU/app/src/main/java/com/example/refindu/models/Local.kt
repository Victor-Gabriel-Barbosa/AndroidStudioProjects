package com.example.refindu.models

import android.net.Uri

// Classe que representa um local salvo
data class Local(
    val id: String = "",
    val uid: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Double = 0.0,
    val name: String = "",
    val category: String = "",
    val imageUrl: String? = null,
    val imageUri: Uri? = null
)