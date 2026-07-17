package com.example.petscue.ui.mensajes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.petscue.data.model.Conversation
import com.example.petscue.ui.theme.PetscueBlue
import com.example.petscue.ui.theme.PetscueBlueDark
import com.example.petscue.ui.theme.PetscueLightSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton

@Composable
fun MensajesScreen(
    onConversationClick: (String) -> Unit,
    viewModel: MensajesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var query by rememberSaveable {
        mutableStateOf("")
    }

    var selectedFilter by rememberSaveable {
        mutableStateOf("Todos")
    }

    val conversations = uiState.conversations

    val filteredConversations = remember(
        conversations,
        query,
        selectedFilter,
        uiState.currentUserId
    ) {
        conversations
            .filter { conversation ->
                val unreadCount =
                    conversation.unreadCountByUser[uiState.currentUserId] ?: 0

                val matchesQuery =
                    conversation.petName.contains(query, ignoreCase = true) ||
                            conversation.otherUserPreviewName.contains(
                                query,
                                ignoreCase = true
                            ) ||
                            conversation.shelterName.contains(
                                query,
                                ignoreCase = true
                            )

                val matchesFilter = when (selectedFilter) {
                    "Sin leer" -> unreadCount > 0
                    "Adopción" -> conversation.type == "ADOPTION"
                    "Avisos" -> conversation.type == "LOST_PET_ALERT"
                    "Preguntas" -> conversation.type == "GENERAL"
                    else -> true
                }

                matchesQuery && matchesFilter
            }
            .sortedByDescending { it.updatedAt }
    }

    val totalUnread = conversations.sumOf { conversation ->
        conversation.unreadCountByUser[uiState.currentUserId] ?: 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MessagesHeader(
            query = query,
            onQueryChange = { query = it },
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it },
            totalUnread = totalUnread
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PetscueBlue
                    )
                }
            }

            uiState.errorMessage != null -> {
                MessagesErrorState(
                    message = uiState.errorMessage
                        ?: "No se pudieron cargar los mensajes."
                )
            }

            filteredConversations.isEmpty() -> {
                EmptyMessagesState(
                    isSearchOrFilterActive =
                        query.isNotBlank() || selectedFilter != "Todos"
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredConversations,
                        key = { conversation -> conversation.id }
                    ) { conversation ->
                        ConversationCard(
                            conversation = conversation,
                            currentUserId = uiState.currentUserId,
                            onClick = {
                                onConversationClick(conversation.id)
                            },
                            onDeleteClick = {
                                viewModel.deleteConversation(conversation.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessagesHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    totalUnread: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 12.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Mensajes",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PetscueBlueDark,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = if (totalUnread > 0) {
                        "Tienes $totalUnread mensaje${if (totalUnread == 1) "" else "s"} sin leer"
                    } else {
                        "Todas tus conversaciones en un mismo lugar"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (totalUnread > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PetscueBlue)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = totalUnread.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Buscar animal, usuario o protectora")
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = PetscueBlue
                )
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PetscueBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = PetscueBlue,
                cursorColor = PetscueBlue,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            horizontalAlignment = Alignment.Start
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "Todos",
                        "Sin leer",
                        "Adopción",
                        "Avisos",
                        "Preguntas"
                    ).forEach { filter ->
                        val selected = selectedFilter == filter

                        AssistChip(
                            onClick = {
                                onFilterSelected(filter)
                            },
                            label = {
                                Text(
                                    text = filter,
                                    fontWeight = if (selected) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Medium
                                    }
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selected) {
                                    PetscueBlue
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                labelColor = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    PetscueBlueDark
                                }
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = if (selected) {
                                    PetscueBlue
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(
    conversation: Conversation,
    currentUserId: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val unreadCount =
        conversation.unreadCountByUser[currentUserId] ?: 0

    val hasUnreadMessages = unreadCount > 0

    var showDeleteDialog by rememberSaveable(conversation.id) {
        mutableStateOf(false)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Eliminar conversación")
            },
            text = {
                Text(
                    "¿Seguro que quieres eliminar esta conversación? " +
                            "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasUnreadMessages) {
                PetscueLightSurfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (hasUnreadMessages) {
                PetscueBlue.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (hasUnreadMessages) 3.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ConversationUserAvatar(
                photoUrl = conversation.otherUserPreviewPhotoUrl,
                userName = conversation.otherUserPreviewName
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = conversation.otherUserPreviewName
                            .ifBlank { "Usuario" },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (hasUnreadMessages) {
                            FontWeight.ExtraBold
                        } else {
                            FontWeight.Bold
                        },
                        color = PetscueBlueDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formatRelativeTime(conversation.lastMessageAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasUnreadMessages) {
                            PetscueBlue
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (hasUnreadMessages) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                }

                Text(
                    text = conversation.lastMessage
                        .ifBlank { "Sin mensajes todavía" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (hasUnreadMessages) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Normal
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ConversationTypeChip(
                        conversation = conversation
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (hasUnreadMessages) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PetscueBlue)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadCount.toString(),
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            showDeleteDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar conversación",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun ConversationUserAvatar(
    photoUrl: String,
    userName: String
) {
    if (photoUrl.isNotBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Foto de perfil de $userName",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(64.dp)
                .height(64.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(64.dp)
                .clip(CircleShape)
                .background(PetscueBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName
                    .firstOrNull()
                    ?.uppercase()
                    ?: "U",
                style = MaterialTheme.typography.titleLarge,
                color = PetscueBlue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Composable
private fun ConversationImage(
    imageUrl: String,
    petName: String
) {
    if (imageUrl.isNotBlank()) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Foto de $petName",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(76.dp)
                .height(76.dp)
                .clip(RoundedCornerShape(18.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .width(76.dp)
                .height(76.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(PetscueBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = PetscueBlue
            )
        }
    }
}

@Composable
private fun ConversationTypeChip(
    conversation: Conversation
) {
    val (text, icon) = when (conversation.type) {
        "ADOPTION" -> {
            "Formulario: ${conversation.adoptionFormStatus ?: "Pendiente"}" to
                    Icons.Default.Description
        }

        "LOST_PET_ALERT" -> {
            "Aviso" to Icons.Default.Description
        }

        else -> {
            "Pregunta" to Icons.Outlined.MailOutline
        }
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(PetscueBlue.copy(alpha = 0.10f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PetscueBlue
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            color = PetscueBlueDark,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyMessagesState(
    isSearchOrFilterActive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MailOutline,
                    contentDescription = null,
                    tint = PetscueBlue,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = if (isSearchOrFilterActive) {
                        "No hay resultados"
                    } else {
                        "Aún no tienes conversaciones"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = PetscueBlueDark,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isSearchOrFilterActive) {
                        "Prueba a cambiar el texto de búsqueda o el filtro."
                    } else {
                        "Cuando alguien pregunte por un animal o un aviso, aparecerá aquí."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MessagesErrorState(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(20.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatRelativeTime(
    timestamp: Long
): String {
    if (timestamp <= 0L) return ""

    val diff = System.currentTimeMillis() - timestamp

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Ahora"
        diff < TimeUnit.HOURS.toMillis(1) ->
            "${TimeUnit.MILLISECONDS.toMinutes(diff)} min"

        diff < TimeUnit.DAYS.toMillis(1) ->
            "${TimeUnit.MILLISECONDS.toHours(diff)} h"

        diff < TimeUnit.DAYS.toMillis(2) -> "Ayer"

        diff < TimeUnit.DAYS.toMillis(7) ->
            "${TimeUnit.MILLISECONDS.toDays(diff)} d"

        else -> SimpleDateFormat(
            "dd/MM",
            Locale.getDefault()
        ).format(Date(timestamp))
    }
}