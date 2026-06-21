package com.example.petscue.ui.notifications

import com.example.petscue.data.model.AppNotification

data class NotificationsUiState(
    val items: List<AppNotification> = emptyList(),
    val unreadCount: Int = 0
)