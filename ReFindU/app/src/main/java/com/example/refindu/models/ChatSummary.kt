package com.example.refindu.models

data class ChatSummary(
    val roomId: String = "",
    val lastMessage: String = "",
    val lastTimestamp: Long = 0,
    val participants: List<String> = emptyList()
)