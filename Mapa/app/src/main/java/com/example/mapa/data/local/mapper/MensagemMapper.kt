package com.example.mapa.data.local.mapper

import com.example.mapa.data.local.entity.MensagemEntity
import com.example.mapa.data.remote.dto.MensagemDTO

fun MensagemDTO.toEntity(salaId: String): MensagemEntity {
    return MensagemEntity(
        id = this.id,
        salaId = salaId,
        autorUid = this.autorUid,
        texto = this.texto,
        timestamp = this.timestamp,
        lido = this.lido,
        imgUrls = this.imgUrls.joinToString(separator = "|")
    )
}

fun MensagemEntity.toDomain(): MensagemDTO {
    return MensagemDTO(
        id = this.id,
        autorUid = this.autorUid,
        texto = this.texto,
        timestamp = this.timestamp,
        lido = this.lido,
        imgUrls = if (this.imgUrls.isBlank()) emptyList() else this.imgUrls.split("|")
    )
}