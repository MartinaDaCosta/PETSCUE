package com.example.petscue.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.petscue.data.model.AppNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsScreen(
    onOpenAlert: (String) -> Unit,
    vm: NotificationsViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    var expanded by remember {
        mutableStateOf(false)
    }

    Box {
        IconButton(
            onClick = {
                expanded = true
            }
        ) {
            BadgedBox(
                badge = {
                    if (uiState.unreadCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(
                                text = if (uiState.unreadCount > 99) {
                                    "99+"
                                } else {
                                    uiState.unreadCount.toString()
                                }
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            },
            modifier = Modifier
                .width(340.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(22.dp)
                )
        ) {
            NotificationsMenuContent(
                notifications = uiState.items,
                onNotificationClick = { item ->
                    vm.markAsRead(item.id)
                    expanded = false

                    if (item.petId.isNotBlank()) {
                        onOpenAlert(item.petId)
                    }
                },
                onEmptyClick = {
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun NotificationsMenuContent(
    notifications: List<AppNotification>,
    onNotificationClick: (AppNotification) -> Unit,
    onEmptyClick: () -> Unit
) {
    if (notifications.isEmpty()) {
        DropdownMenuItem(
            text = {
                EmptyNotificationsContent()
            },
            onClick = onEmptyClick
        )
        return
    }

    Column(
        modifier = Modifier
            .width(340.dp)
            .heightIn(max = 440.dp)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 10.dp,
                top = 10.dp,
                end = 10.dp,
                bottom = 12.dp
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Notificaciones",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = 6.dp,
                vertical = 4.dp
            )
        )

        notifications.forEach { item ->
            NotificationRow(
                item = item,
                onClick = {
                    onNotificationClick(item)
                }
            )
        }
    }
}

@Composable
private fun EmptyNotificationsContent() {
    Row(
        modifier = Modifier.padding(
            horizontal = 6.dp,
            vertical = 10.dp
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = "No tienes notificaciones",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Te avisaremos cuando haya novedades.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotificationRow(
    item: AppNotification,
    onClick: () -> Unit
) {
    val containerColor = if (item.isRead) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }

    val borderColor = if (item.isRead) {
        MaterialTheme.colorScheme.outlineVariant
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
    }

    val titleColor = if (item.isRead) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val bodyColor = if (item.isRead) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(
            1.dp,
            borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.isRead) 0.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NotificationIndicator(
                isRead = item.isRead
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = titleColor,
                    fontWeight = if (item.isRead) {
                        FontWeight.Medium
                    } else {
                        FontWeight.Bold
                    }
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = bodyColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatNotificationDate(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = bodyColor
                )
            }
        }
    }
}

@Composable
private fun NotificationIndicator(
    isRead: Boolean
) {
    Box(
        modifier = Modifier
            .padding(top = 5.dp)
            .size(if (isRead) 9.dp else 11.dp)
            .clip(CircleShape)
            .background(
                if (isRead) {
                    MaterialTheme.colorScheme.outline
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
    )
}

private fun formatNotificationDate(
    timestamp: Long
): String {
    if (timestamp == 0L) {
        return "Ahora"
    }

    return SimpleDateFormat(
        "dd/MM HH:mm",
        Locale.getDefault()
    ).format(Date(timestamp))
}