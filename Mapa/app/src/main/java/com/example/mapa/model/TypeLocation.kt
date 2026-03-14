package com.example.mapa.model

import com.example.mapa.R

/**
 * Enum que representa o tipo de local.
 */
enum class TypeLocation(val id: String, val texto: Int) {
    LOST("Perdido", R.string.perdido),
    FOUND("Encontrado", R.string.encontrado);

    companion object {
        fun fromId(id: String): TypeLocation {
            return entries.find { it.id == id } ?: LOST
        }
    }
}