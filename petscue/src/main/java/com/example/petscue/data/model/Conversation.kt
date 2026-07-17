package com.example.petscue.data.model

enum class ConversationType {
    GENERAL,
    LOST_PET_ALERT,
    ADOPTION
}

data class Conversation(
    val id: String = "",
    val type: String = ConversationType.GENERAL.name,

    val participantIds: List<String> = emptyList(),

    val createdBy: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,

    val lastMessage: String = "",
    val lastMessageAt: Long = 0L,
    val lastMessageSenderId: String = "",

    val unreadCountByUser: Map<String, Int> = emptyMap(),

    val petId: String? = null,
    val petName: String = "",
    val petImageUrl: String = "",
    val petOwnerId: String = "",

    val shelterId: String? = null,
    val shelterName: String = "",

    val postId: String? = null,
    val alertId: String? = null,

    val adoptionFormId: String? = null,
    val hasAdoptionForm: Boolean = false,
    val adoptionFormStatus: String? = null,

    val otherUserPreviewName: String = "",
    val otherUserPreviewPhotoUrl: String = "",
    val hiddenForUserIds: List<String> = emptyList()


)