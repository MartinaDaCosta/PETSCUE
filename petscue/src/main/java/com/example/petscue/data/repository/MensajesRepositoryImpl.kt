package com.example.petscue.data.repository

import com.example.petscue.data.model.ChatMessage
import com.example.petscue.data.model.Conversation
import com.example.petscue.data.model.ConversationType
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class MensajesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MensajesRepository {

    private val conversationsRef = firestore.collection("conversations")

    override fun observeConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val registration = conversationsRef
            .whereArrayContains("participantIds", userId)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Conversation::class.java)?.copy(id = doc.id)
                }.orEmpty()

                trySend(conversations).isSuccess
            }

        awaitClose { registration.remove() }
    }

    override fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = conversationsRef
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                }.orEmpty()

                trySend(messages).isSuccess
            }

        awaitClose { registration.remove() }
    }

    override suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        text: String
    ) {
        val conversationRef = conversationsRef.document(conversationId)
        val conversationSnapshot = conversationRef.get().await()
        val conversation = conversationSnapshot.toObject(Conversation::class.java)
            ?.copy(id = conversationSnapshot.id)
            ?: error("Conversation not found")

        val messageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val message = ChatMessage(
            id = messageId,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            text = text,
            createdAt = now,
            readBy = listOf(senderId)
        )

        conversationRef.collection("messages")
            .document(messageId)
            .set(message)
            .await()

        val updatedUnreadMap = conversation.participantIds.associateWith { participantId ->
            if (participantId == senderId) {
                0
            } else {
                (conversation.unreadCountByUser[participantId] ?: 0) + 1
            }
        }

        conversationRef.update(
            mapOf(
                "lastMessage" to text,
                "lastMessageAt" to now,
                "lastMessageSenderId" to senderId,
                "updatedAt" to now,
                "unreadCountByUser" to updatedUnreadMap
            )
        ).await()
    }

    override suspend fun createOrGetGeneralConversation(
        currentUserId: String,
        otherUserId: String,
        petId: String?,
        petName: String,
        petImageUrl: String,
        petOwnerId: String,
        postId: String?,
        alertId: String?
    ): String {
        val otherUser = getUser(otherUserId)

        val type = if (alertId != null) {
            ConversationType.LOST_PET_ALERT.name
        } else {
            ConversationType.GENERAL.name
        }

        val existing = conversationsRef
            .whereEqualTo("type", type)
            .whereArrayContains("participantIds", currentUserId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Conversation::class.java)?.copy(id = doc.id)
            }
            .firstOrNull { conversation ->
                conversation.participantIds.contains(otherUserId) &&
                        conversation.petId == petId &&
                        conversation.alertId == alertId
            }

        if (existing != null) return existing.id

        val conversationId = conversationsRef.document().id
        val now = System.currentTimeMillis()

        val conversation = Conversation(
            id = conversationId,
            type = type,
            participantIds = listOf(currentUserId, otherUserId),
            createdBy = currentUserId,
            createdAt = now,
            updatedAt = now,
            lastMessage = "",
            lastMessageAt = 0L,
            lastMessageSenderId = "",
            unreadCountByUser = mapOf(
                currentUserId to 0,
                otherUserId to 0
            ),
            petId = petId,
            petName = petName,
            petImageUrl = petImageUrl,
            petOwnerId = petOwnerId,
            shelterId = if (otherUser.role == UserRole.PROTECTORA) otherUser.uid else null,
            shelterName = if (otherUser.role == UserRole.PROTECTORA) otherUser.nombreProtectora else "",
            postId = postId,
            alertId = alertId,
            adoptionFormId = null,
            hasAdoptionForm = false,
            adoptionFormStatus = null,
            otherUserPreviewName = otherUser.username,
            otherUserPreviewPhotoUrl = otherUser.photoUrl
        )

        conversationsRef.document(conversationId).set(conversation).await()
        return conversationId
    }

    override suspend fun createOrGetAdoptionConversation(
        currentUserId: String,
        shelterId: String,
        petId: String,
        petName: String,
        petImageUrl: String,
        adoptionFormId: String?,
        adoptionFormStatus: String?
    ): String {
        val shelter = getUser(shelterId)

        require(shelter.role == UserRole.PROTECTORA) {
            "Solo una protectora puede recibir conversaciones de adopción"
        }

        val existing = conversationsRef
            .whereEqualTo("type", ConversationType.ADOPTION.name)
            .whereArrayContains("participantIds", currentUserId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(Conversation::class.java)?.copy(id = doc.id)
            }
            .firstOrNull { conversation ->
                conversation.participantIds.contains(shelterId) &&
                        conversation.petId == petId
            }

        if (existing != null) return existing.id

        val conversationId = conversationsRef.document().id
        val now = System.currentTimeMillis()

        val conversation = Conversation(
            id = conversationId,
            type = ConversationType.ADOPTION.name,
            participantIds = listOf(currentUserId, shelterId),
            createdBy = currentUserId,
            createdAt = now,
            updatedAt = now,
            lastMessage = "",
            lastMessageAt = 0L,
            lastMessageSenderId = "",
            unreadCountByUser = mapOf(
                currentUserId to 0,
                shelterId to 0
            ),
            petId = petId,
            petName = petName,
            petImageUrl = petImageUrl,
            petOwnerId = shelterId,
            shelterId = shelterId,
            shelterName = shelter.nombreProtectora,
            postId = null,
            alertId = null,
            adoptionFormId = adoptionFormId,
            hasAdoptionForm = adoptionFormId != null,
            adoptionFormStatus = adoptionFormStatus,
            otherUserPreviewName = shelter.username,
            otherUserPreviewPhotoUrl = shelter.photoUrl
        )

        conversationsRef.document(conversationId).set(conversation).await()
        return conversationId
    }

    override suspend fun markConversationAsRead(
        conversationId: String,
        userId: String
    ) {
        conversationsRef.document(conversationId)
            .update("unreadCountByUser.$userId", 0)
            .await()
    }

    private suspend fun getUser(userId: String): User {
        return firestore.collection("users")
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
            ?.copy(uid = userId)
            ?: error("User not found: $userId")
    }
}