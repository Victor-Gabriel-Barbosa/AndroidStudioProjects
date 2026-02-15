package com.example.mapa.data.local.mapper

import com.example.mapa.data.local.entity.UsuarioEntity
import com.example.mapa.data.remote.dto.Usuario

fun Usuario.toEntity() = UsuarioEntity(
    id = this.uid,
    nome = this.nome,
    email = this.email,
    fotoUrl = this.foto
)

fun UsuarioEntity.toDomain() = Usuario(
    uid = this.id,
    nome = this.nome,
    email = this.email,
    foto = this.fotoUrl
)