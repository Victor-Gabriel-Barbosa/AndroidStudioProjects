package com.example.mapa.data.mapper

import com.example.mapa.data.local.entity.UserEntity
import com.example.mapa.data.remote.dto.UserDTO

fun UserDTO.toEntity() = UserEntity(
    uid = this.uid,
    name = this.name,
    email = this.email,
    photoUrl = this.photo,
    averageRating = this.averageRating,
    ratingCount = this.ratingCount,
    reviewerUids = this.reviewerUids.joinToString("|"),
    fcmToken = this.fcmToken
)

fun UserEntity.toDTO() = UserDTO(
    uid = this.uid,
    name = this.name,
    email = this.email,
    photo = this.photoUrl,
    averageRating = this.averageRating,
    ratingCount = this.ratingCount,
    reviewerUids = if (this.reviewerUids.isBlank()) emptyList() else this.reviewerUids.split("|"),
    fcmToken = this.fcmToken
)