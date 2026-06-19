package com.example.petscue.data.repository

import com.example.petscue.data.model.ChatMessage
import com.example.petscue.data.model.Conversation
import kotlinx.coroutines.flow.Flow

interface MensajesRepository {
    fun observeConversations(userId: String): Flow<List<Conversation>>
    fun observeMessages(conversationId: String): Flow<List<ChatMessage>>

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        text: String
    )

    suspend fun createOrGetGeneralConversation(
        currentUserId: String,
        otherUserId: String,
        petId: String? = null,
        petName: String = "",
        petImageUrl: String = "",
        petOwnerId: String = "",
        postId: String? = null,
        alertId: String? = null
    ): String

    suspend fun createOrGetAdoptionConversation(
        currentUserId: String,
        shelterId: String,
        petId: String,
        petName: String,
        petImageUrl: String,
        adoptionFormId: String? = null,
        adoptionFormStatus: String? = null
    ): String

    suspend fun markConversationAsRead(
        conversationId: String,
        userId: String
    )
}