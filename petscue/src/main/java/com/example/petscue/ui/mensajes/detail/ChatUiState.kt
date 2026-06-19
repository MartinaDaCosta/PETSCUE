package com.example.petscue.ui.mensajes.detail

import com.example.petscue.data.model.ChatMessage
import com.example.petscue.data.model.Conversation

data class ChatUiState(
    val isLoading: Boolean = true,
    val conversation: Conversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val currentUserId: String = "",
    val errorMessage: String? = null
)