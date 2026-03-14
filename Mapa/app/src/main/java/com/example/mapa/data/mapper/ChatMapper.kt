package com.example.mapa.data.mapper

import com.example.mapa.data.local.entity.ChatEntity
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.MsgDTO

fun ChatDTO.toEntity(): ChatEntity {
    return ChatEntity(
        id = this.id,
        lastTimestamp = this.lastTimestamp,
        participants = this.participants.joinToString(separator = "|"),
        visibleTo = this.visibleTo.joinToString(separator = "|"),
        lastMsgUid = this.lastMsg?.uid,
        lastMsgText = this.lastMsg?.text,
        lastMsgTimestamp = this.lastMsg?.timestamp,
        lastMsgRead = this.lastMsg?.read,
        locationId = this.locationId
    )
}

fun ChatEntity.toDTO(): ChatDTO {
    val ultimaMsg = if (lastMsgUid != null) {
        MsgDTO(
            uid = lastMsgUid,
            text = lastMsgText ?: "",
            timestamp = lastMsgTimestamp ?: 0L,
            read = lastMsgRead ?: false
        )
    } else null

    return ChatDTO(
        id = this.id,
        lastTimestamp = this.lastTimestamp,
        participants = if (this.participants.isBlank()) emptyList() else this.participants.split("|"),
        visibleTo = if (this.visibleTo.isBlank()) emptyList() else this.visibleTo.split("|"),
        lastMsg = ultimaMsg,
        locationId = this.locationId
    )
}