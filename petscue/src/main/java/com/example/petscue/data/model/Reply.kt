package com.example.petscue.data.model

data class Reply(
    val id: String = "",
    val postId: String = "",
    val parentReplyId: String? = null,
    val userId: String = "",
    val userName: String = "",
    val userHandle: String = "",
    val userAvatar: String = "",
    val mensaje: String = "",
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(),
    val sharedBy: List<String> = emptyList()
)