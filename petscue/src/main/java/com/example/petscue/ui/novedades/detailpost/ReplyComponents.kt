@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.petscue.ui.novedades.detailpost

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.Reply
import com.example.petscue.ui.novedades.tiempoRelativo

private val BluePrimary = Color(0xFF4A90E2)
private val BlueBorder = Color(0xFF6CA9F0)
private val DeleteRed  = Color(0xFFD32F2F)

// ─────────────────────────────────────────────
// Tarjeta del post principal
// ─────────────────────────────────────────────
@Composable
fun PostDetailCard(
    post: Post,
    isLiked: Boolean,
    isLiking: Boolean,
    onToggleLike: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, BlueBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        color = BluePrimary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = post.userName.firstOrNull()?.uppercase() ?: "U",
                                color = BluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = post.userId.isNotBlank()) {
                            onOpenProfile(post.userId)
                        }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.userName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
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
                                color = BluePrimary,
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
                Text(text = post.mensaje, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (post.fotos.isNotEmpty()) {
                PostImages(post.fotos.take(4))
                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(color = BlueBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DetailAction(Icons.Default.ChatBubbleOutline, post.comentarios.toString())
                DetailAction(Icons.Default.Repeat, "Repost")
                LikeDetailAction(
                    isLiked = isLiked,
                    likesCount = post.likes,
                    isLoading = isLiking,
                    onClick = onToggleLike
                )
                DetailAction(Icons.Default.Share, "Compartir")
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(enabled = !isLoading) { onClick() }
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = if (isLiked) DeleteRed else MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Me gusta",
                tint = if (isLiked) DeleteRed else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = likesCount.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isLiked) DeleteRed else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
// ─────────────────────────────────────────────
// Hilo de comentario: raíz + hijos indentados
// ─────────────────────────────────────────────
@Composable
fun ReplyThreadItem(
    reply: Reply,
    childReplies: List<Reply>,
    currentUserId: String,
    onReplyClick: (Reply) -> Unit,
    onDeleteReply: (Reply) -> Unit,
    onOpenProfile: (String) -> Unit
) {
    ReplyCard(
        reply = reply,
        canDelete = reply.userId == currentUserId,
        onReplyClick = { onReplyClick(reply) },
        onDeleteClick = { onDeleteReply(reply) },
        onOpenProfile = onOpenProfile,
        startPadding = 14.dp
    )

    childReplies.forEach { child ->
        ReplyCard(
            reply = child,
            canDelete = child.userId == currentUserId,
            onReplyClick = { onReplyClick(child) },
            onDeleteClick = { onDeleteReply(child) },
            onOpenProfile = onOpenProfile,
            startPadding = 34.dp,
            isChild = true
        )
    }
}

// ─────────────────────────────────────────────
// Tarjeta individual de comentario
// ─────────────────────────────────────────────
@Composable
private fun ReplyCard(
    reply: Reply,
    canDelete: Boolean,
    onReplyClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onOpenProfile: (String) -> Unit,
    startPadding: Dp = 0.dp,
    isChild: Boolean = false
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eliminar comentario") },
            text = { Text("¿Seguro que quieres borrar este comentario?") },
            confirmButton = {
                TextButton(onClick = { showDialog = false; onDeleteClick() }) {
                    Text("Eliminar", color = DeleteRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding, top = 6.dp, end = 14.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isChild) BlueBorder.copy(alpha = 0.35f) else BlueBorder.copy(alpha = 0.6f)
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    UserHeader(
                        userId = reply.userId,
                        avatar = reply.userAvatar,
                        userName = reply.userName,
                        userHandle = reply.userHandle,
                        onOpenProfile = onOpenProfile
                    )
                }

                if (canDelete) {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar comentario",
                            tint = DeleteRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (reply.mensaje.isNotBlank()) {
                Text(
                    text = reply.mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(color = BlueBorder.copy(alpha = 0.35f))
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
                    icon = Icons.Default.FavoriteBorder,
                    text = reply.likes.toString()
                )
                DetailAction(
                    icon = Icons.Default.Share,
                    text = "Compartir"
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Composer de respuesta (barra inferior)
// ─────────────────────────────────────────────
@Composable
fun ReplyComposer(
    text          : String,
    replyingTo    : Reply?,
    isSending     : Boolean,
    onTextChange  : (String) -> Unit,
    onCancelReply : () -> Unit,
    onSend        : () -> Unit
) {
    Surface(color = Color.White, shadowElevation = 10.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            replyingTo?.let { r ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = "Respondiendo a ${r.userHandle.ifBlank { r.userName }}",
                        modifier = Modifier.weight(1f),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = BluePrimary
                    )
                    TextButton(onClick = onCancelReply) { Text("Cancelar") }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Row(verticalAlignment = Alignment.Bottom) {
                OutlinedTextField(
                    value         = text,
                    onValueChange = onTextChange,
                    modifier      = Modifier.weight(1f),
                    placeholder   = { Text("Publica tu respuesta") },
                    maxLines      = 4,
                    shape         = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank() && !isSending
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar respuesta",
                            tint               = BluePrimary
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Componentes internos compartidos
// ─────────────────────────────────────────────
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
                .clickable { onOpenProfile(userId) }
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
                color = BluePrimary.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "U",
                        color = BluePrimary,
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
                    if (enabled) Modifier.clickable { onOpenProfile(userId) } else Modifier
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
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
    icon    : ImageVector,
    text    : String,
    onClick : (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier          = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = text,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text  = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun PostImages(images: List<String>) {
    when (images.size) {
        0    -> Unit
        1    -> AsyncImage(
            model              = images.first(),
            contentDescription = "Imagen del post",
            modifier           = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
        else -> Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier            = Modifier.fillMaxWidth()
        ) {
            images.chunked(2).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    row.forEach { img ->
                        AsyncImage(
                            model              = img,
                            contentDescription = "Imagen del post",
                            modifier           = Modifier
                                .weight(1f)
                                .height(170.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}