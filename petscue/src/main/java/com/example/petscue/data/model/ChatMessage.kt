package com.example.petscue.data.model

data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val type: String = "TEXT",
    val createdAt: Long = 0L,
    val readBy: List<String> = emptyList()
)