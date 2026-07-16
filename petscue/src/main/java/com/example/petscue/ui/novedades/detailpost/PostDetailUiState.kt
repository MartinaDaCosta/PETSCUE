package com.example.petscue.ui.novedades.detailpost

import com.example.petscue.data.model.Post
import com.example.petscue.data.model.Reply

data class PostDetailUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isLiking: Boolean = false,
    val currentUserId: String = "",
    val post: Post? = null,
    val replies: List<Reply> = emptyList(),
    val replyText: String = "",
    val replyingTo: Reply? = null,
    val shouldFocusReply: Boolean = false,
    val error: String? = null
) {
    val isLikedByCurrentUser: Boolean
        get() = post?.likedBy?.contains(currentUserId) == true
}