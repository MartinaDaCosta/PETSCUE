package com.example.petscue.data.model

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val alertId: String = "",
    val petId: String = "",
    val senderId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)