package com.example.mapa.data.mapper

import com.example.mapa.data.local.entity.UsuarioEntity
import com.example.mapa.data.remote.dto.UsuarioDTO

fun UsuarioDTO.toEntity() = UsuarioEntity(
    id = this.uid,
    nome = this.nome,
    email = this.email,
    fotoUrl = this.foto,
    notaMedia = this.notaMedia,
    notaQtd = this.notaQtd,
    avaliadores = this.avaliadores.joinToString("|"),
    fcmToken = this.fcmToken
)

fun UsuarioEntity.toDTO() = UsuarioDTO(
    uid = this.id,
    nome = this.nome,
    email = this.email,
    foto = this.fotoUrl,
    notaMedia = this.notaMedia,
    notaQtd = this.notaQtd,
    avaliadores = if (this.avaliadores.isBlank()) emptyList() else this.avaliadores.split("|"),
    fcmToken = this.fcmToken
)