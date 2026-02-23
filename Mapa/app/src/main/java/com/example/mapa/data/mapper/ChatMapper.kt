package com.example.mapa.data.mapper

import com.example.mapa.data.local.entity.ChatEntity
import com.example.mapa.data.remote.dto.ChatDTO
import com.example.mapa.data.remote.dto.MensagemDTO

fun ChatDTO.toEntity(): ChatEntity {
    return ChatEntity(
        salaId = this.salaId,
        ultimoTimestamp = this.ultimoTimestamp,
        participantes = this.participantes.joinToString(separator = "|"),
        visivelPara = this.visivelPara.joinToString(separator = "|"),
        ultimaMsgAutorUid = this.ultimaMsg?.autorUid,
        ultimaMsgTexto = this.ultimaMsg?.texto,
        ultimaMsgTimestamp = this.ultimaMsg?.timestamp,
        ultimaMsgLido = this.ultimaMsg?.lido,
        localId = this.localId
    )
}

fun ChatEntity.toDomain(): ChatDTO {
    val ultimaMsg = if (ultimaMsgAutorUid != null) {
        MensagemDTO(
            autorUid = ultimaMsgAutorUid,
            texto = ultimaMsgTexto ?: "",
            timestamp = ultimaMsgTimestamp ?: 0L,
            lido = ultimaMsgLido ?: false
        )
    } else null

    return ChatDTO(
        salaId = this.salaId,
        ultimoTimestamp = this.ultimoTimestamp,
        participantes = if (this.participantes.isBlank()) emptyList() else this.participantes.split("|"),
        visivelPara = if (this.visivelPara.isBlank()) emptyList() else this.visivelPara.split("|"),
        ultimaMsg = ultimaMsg,
        localId = this.localId
    )
}