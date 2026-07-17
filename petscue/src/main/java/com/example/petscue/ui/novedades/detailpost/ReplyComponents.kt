@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.petscue.ui.novedades.detailpost

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.Reply
import com.example.petscue.ui.novedades.tiempoRelativo

@Composable
fun PostDetailCard(
    post: Post,
    isLiked: Boolean,
    isReposted: Boolean,
    isLiking: Boolean,
    onCommentClick: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleRepost: () -> Unit,
    onShare: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                val profileModifier = Modifier.clickable(
                    enabled = post.userId.isNotBlank()
                ) {
                    onOpenProfile(post.userId)
                }

                if (post.userAvatar.isNotBlank()) {
                    AsyncImage(
                        model = post.userAvatar,
                        contentDescription = "Avatar de ${post.userName}",
                        modifier = profileModifier
                            .size(42.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = profileModifier
                            .size(42.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = post.userName
                                    .firstOrNull()
                                    ?.uppercase()
                                    ?: "U",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = post.userId.isNotBlank()
                        ) {
                            onOpenProfile(post.userId)
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = post.userName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (post.userHandle.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = post.userHandle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (post.tipo.isNotBlank()) {
                            Text(
                                text = post.tipo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "·",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = tiempoRelativo(post.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (post.ubicacion.isNotBlank()) {
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = post.ubicacion,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (post.mensaje.isNotBlank()) {
                Text(
                    text = post.mensaje,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            if (post.fotos.isNotEmpty()) {
                PostImages(post.fotos.take(4))
                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DetailAction(
                    icon = Icons.Default.ChatBubbleOutline,
                    text = post.comentarios.toString(),
                    onClick = onCommentClick
                )

                DetailAction(
                    icon = Icons.Default.Repeat,
                    text = if (isReposted) "Publicado" else "Repost",
                    selected = isReposted,
                    onClick = onToggleRepost
                )

                LikeDetailAction(
                    isLiked = isLiked,
                    likesCount = post.likes,
                    isLoading = isLiking,
                    onClick = onToggleLike
                )

                DetailAction(
                    icon = Icons.Default.Share,
                    text = "Compartir",
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${post.userName}: ${post.mensaje}"
                            )
                        }

                        context.startActivity(
                            Intent.createChooser(
                                sendIntent,
                                "Compartir publicación"
                            )
                        )

                        onShare()
                    }
                )
            }
        }
    }
}

@Composable
internal fun LikeDetailAction(
    isLiked: Boolean,
    likesCount: Int,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val iconColor = if (isLiked) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(
            enabled = !isLoading
        ) {
            onClick()
        }
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = iconColor
            )
        } else {
            Icon(
                imageVector = if (isLiked) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Default.FavoriteBorder
                },
                contentDescription = "Me gusta",
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = likesCount.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = iconColor
        )
    }
}

@Composable
fun ReplyThreadItem(
    reply: Reply,
    childReplies: List<Reply>,
    currentUserId: String,
    onReplyClick: (Reply) -> Unit,
    onDeleteReply: (Reply) -> Unit,
    onToggleLike: (Reply) -> Unit,
    onShare: (Reply) -> Unit,
    onOpenProfile: (String) -> Unit
) {
    ReplyCard(
        reply = reply,
        canDelete = reply.userId == currentUserId,
        isLiked = currentUserId in reply.likedBy,
        onReplyClick = {
            onReplyClick(reply)
        },
        onDeleteClick = {
            onDeleteReply(reply)
        },
        onLikeClick = {
            onToggleLike(reply)
        },
        onShareClick = {
            onShare(reply)
        },
        onOpenProfile = onOpenProfile,
        startPadding = 14.dp
    )

    childReplies.forEach { child ->
        ReplyCard(
            reply = child,
            canDelete = child.userId == currentUserId,
            isLiked = currentUserId in child.likedBy,
            onReplyClick = {
                onReplyClick(child)
            },
            onDeleteClick = {
                onDeleteReply(child)
            },
            onLikeClick = {
                onToggleLike(child)
            },
            onShareClick = {
                onShare(child)
            },
            onOpenProfile = onOpenProfile,
            startPadding = 34.dp,
            isChild = true
        )
    }
}

@Composable
private fun ReplyCard(
    reply: Reply,
    canDelete: Boolean,
    isLiked: Boolean,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onLikeClick: () -> Unit,
    onShareClick: () -> Unit,
    onOpenProfile: (String) -> Unit,
    startPadding: Dp = 0.dp,
    isChild: Boolean = false
) {
    var showDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text("Eliminar comentario")
            },
            text = {
                Text("¿Seguro que quieres borrar este comentario?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
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
                        showDialog = false
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
            .padding(
                start = startPadding,
                top = 6.dp,
                end = 14.dp
            ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isChild) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isChild) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    UserHeader(
                        userId = reply.userId,
                        avatar = reply.userAvatar,
                        userName = reply.userName,
                        userHandle = reply.userHandle,
                        onOpenProfile = onOpenProfile
                    )
                }

                if (canDelete) {
                    IconButton(
                        onClick = {
                            showDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar comentario",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (reply.mensaje.isNotBlank()) {
                Text(
                    text = reply.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailAction(
                    icon = Icons.Default.ChatBubbleOutline,
                    text = "Responder",
                    onClick = onReplyClick
                )
                DetailAction(
                    icon = if (isLiked) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    text = reply.likes.toString(),
                    selected = isLiked,
                    onClick = onLikeClick
                )

                DetailAction(
                    icon = Icons.Default.Share,
                    text = "Compartir",
                    onClick = onShareClick
                )
            }
        }
    }
}

@Composable
fun ReplyComposer(
    text: String,
    replyingTo: Reply?,
    isSending: Boolean,
    requestFocus: Boolean,
    onFocusConsumed: () -> Unit,
    onTextChange: (String) -> Unit,
    onCancelReply: () -> Unit,
    onSend: () -> Unit
) {
    val focusRequester = remember {
        FocusRequester()
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
            onFocusConsumed()
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                )
        ) {
            replyingTo?.let { reply ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Respondiendo a ${
                            reply.userHandle.ifBlank {
                                reply.userName
                            }
                        }",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TextButton(
                        onClick = onCancelReply
                    ) {
                        Text("Cancelar")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text("Publica tu respuesta")
                    },
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank() && !isSending
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar respuesta",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun UserHeader(
    userId: String,
    avatar: String,
    userName: String,
    userHandle: String,
    extra: String = "",
    onOpenProfile: (String) -> Unit
) {
    val enabled = userId.isNotBlank()

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        val avatarModifier = if (enabled) {
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable {
                    onOpenProfile(userId)
                }
        } else {
            Modifier
                .size(42.dp)
                .clip(CircleShape)
        }

        if (avatar.isNotBlank()) {
            AsyncImage(
                model = avatar,
                contentDescription = "Avatar de $userName",
                modifier = avatarModifier,
                contentScale = ContentScale.Crop
            )
        } else {
            Surface(
                modifier = avatarModifier,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName
                            .firstOrNull()
                            ?.uppercase()
                            ?: "U",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (enabled) {
                        Modifier.clickable {
                            onOpenProfile(userId)
                        }
                    } else {
                        Modifier
                    }
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (userHandle.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = userHandle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (extra.isNotBlank()) {
                Text(
                    text = extra,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun DetailAction(
    icon: ImageVector,
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val tint = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (onClick != null) {
            Modifier.clickable {
                onClick()
            }
        } else {
            Modifier
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}

@Composable
internal fun PostImages(
    images: List<String>
) {
    when (images.size) {
        0 -> Unit

        1 -> {
            AsyncImage(
                model = images.first(),
                contentDescription = "Imagen del post",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )
        }

        else -> {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                images.chunked(2).forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Imagen del post",
                                modifier = Modifier
                                    .weight(1f)
                                    .height(170.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        if (row.size == 1) {
                            Spacer(
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}