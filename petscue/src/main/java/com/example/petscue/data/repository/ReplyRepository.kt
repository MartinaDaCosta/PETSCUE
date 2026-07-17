package com.example.petscue.data.repository

import com.example.petscue.data.model.Reply
import kotlinx.coroutines.flow.Flow

interface ReplyRepository {
    fun getReplies(postId: String): Flow<List<Reply>>
    suspend fun insertReply(postId: String, reply: Reply)
    suspend fun deleteReply(postId: String, replyId: String)
    suspend fun toggleReplyLike(
        postId: String,
        replyId: String,
        userId: String
    )

    suspend fun toggleReplyShare(
        postId: String,
        replyId: String,
        userId: String
    )
}