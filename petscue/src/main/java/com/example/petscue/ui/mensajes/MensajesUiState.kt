package com.example.petscue.ui.mensajes

import com.example.petscue.data.model.Conversation

data class MensajesUiState(
    val isLoading: Boolean = true,
    val conversations: List<Conversation> = emptyList(),
    val currentUserId: String = "",
    val errorMessage: String? = null
)