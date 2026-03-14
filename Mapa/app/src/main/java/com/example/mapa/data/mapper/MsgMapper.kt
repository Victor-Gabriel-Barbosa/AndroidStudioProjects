package com.example.mapa.data.mapper

import com.example.mapa.data.local.entity.MsgEntity
import com.example.mapa.data.remote.dto.MsgDTO

fun MsgDTO.toEntity(salaId: String): MsgEntity {
    return MsgEntity(
        id = this.id,
        chatId = salaId,
        uid = this.uid,
        text = this.text,
        timestamp = this.timestamp,
        read = this.read,
        imgUrls = this.imgUrls.joinToString(separator = "|")
    )
}

fun MsgEntity.toDTO(): MsgDTO {
    return MsgDTO(
        id = this.id,
        uid = this.uid,
        text = this.text,
        timestamp = this.timestamp,
        read = this.read,
        imgUrls = if (this.imgUrls.isBlank()) emptyList() else this.imgUrls.split("|")
    )
}