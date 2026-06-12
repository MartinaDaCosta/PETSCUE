package com.example.petscue.ui.novedades.detailpost

import com.example.petscue.data.model.Post
import com.example.petscue.data.model.Reply

data class PostDetailUiState(
    val post: Post? = null,
    val replies: List<Reply> = emptyList(),
    val replyText: String = "",
    val replyingTo: Reply? = null,
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null
)