package com.example.mapa.data.local.mapper

import com.example.mapa.data.local.entity.LocalEntity
import com.example.mapa.data.remote.dto.Local
import java.util.Date

fun Local.toEntity() = LocalEntity(
    id = this.id,
    uid = this.uid,
    nome = this.nome,
    tipo = this.tipo,
    descricao = this.descricao,
    latitude = this.latitude,
    longitude = this.longitude,
    raio = this.raio,
    data = this.data?.time,
    imgUrls = this.imgUrls.joinToString(separator = "|")
)

fun LocalEntity.toDomain() = Local(
    id = this.id,
    uid = this.uid,
    nome = this.nome,
    tipo = this.tipo,
    descricao = this.descricao,
    latitude = this.latitude,
    longitude = this.longitude,
    raio = this.raio,
    data = this.data?.let { Date(it) },
    imgUrls = if (this.imgUrls.isBlank()) emptyList() else this.imgUrls.split("|")
)