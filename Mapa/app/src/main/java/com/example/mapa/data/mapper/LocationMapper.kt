package com.example.mapa.data.mapper

import com.example.mapa.data.local.entity.LocationEntity
import com.example.mapa.data.remote.dto.LocationDTO
import java.util.Date

fun LocationDTO.toEntity() = LocationEntity(
    id = this.id,
    uid = this.uid,
    name = this.name,
    type = this.type,
    description = this.description,
    latitude = this.latitude,
    longitude = this.longitude,
    radius = this.radius,
    date = this.date?.time,
    imgUrls = this.imgUrls.joinToString(separator = "|")
)

fun LocationEntity.toDTO() = LocationDTO(
    id = this.id,
    uid = this.uid,
    name = this.name,
    type = this.type,
    description = this.description,
    latitude = this.latitude,
    longitude = this.longitude,
    radius = this.radius,
    date = this.date?.let { Date(it) },
    imgUrls = if (this.imgUrls.isBlank()) emptyList() else this.imgUrls.split("|")
)