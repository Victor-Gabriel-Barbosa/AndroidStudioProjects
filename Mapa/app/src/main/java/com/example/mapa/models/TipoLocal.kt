package com.example.mapa.models

import com.example.mapa.R

/**
 * Enum que representa o tipo de local.
 */
enum class TipoLocal(val id: String, val texto: Int) {
    PERDIDO("Perdido", R.string.perdido),
    ENCONTRADO("Encontrado", R.string.encontrado);

    companion object {
        fun fromId(id: String): TipoLocal {
            return entries.find { it.id == id } ?: PERDIDO
        }
    }
}