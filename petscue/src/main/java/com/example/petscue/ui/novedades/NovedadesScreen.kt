@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.petscue.ui.novedades

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.petscue.data.model.Post
import java.util.UUID

private val BluePrimary = Color(0xFF4A90E2)
private val BlueBorder = Color(0xFF6CA9F0)
private val CardBackground = Color(0xFFF7F7F8)

@Composable
fun NovedadesScreen(
    viewModel: NovedadesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser

    val currentUserName = listOf(currentUser.nombre, currentUser.apellido)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Usuario Petscue" }

    val currentUserHandle = currentUser.username
        .trim()
        .let { if (it.isBlank()) "@usuario" else "@$it" }

    var mostrarComposer by rememberSaveable { mutableStateOf(false) }
    var imagenSeleccionada by rememberSaveable { mutableStateOf<String?>(null) }
    var postAComentar by rememberSaveable { mutableStateOf<Post?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imagenSeleccionada = it.toString()
            mostrarComposer = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }

            uiState.posts.isEmpty() -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    CrearPublicacionCard(
                        photoUrl = currentUser.photoUrl,
                        displayName = currentUserName,
                        onTextClick = {
                            imagenSeleccionada = null
                            mostrarComposer = true
                        },
                        onPhotoClick = { galleryLauncher.launch("image/*") }
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No hay novedades aún",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sé el primero en publicar una novedad",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        CrearPublicacionCard(
                            photoUrl = currentUser.photoUrl,
                            displayName = currentUserName,
                            onTextClick = {
                                imagenSeleccionada = null
                                mostrarComposer = true
                            },
                            onPhotoClick = { galleryLauncher.launch("image/*") }
                        )
                    }

                    items(uiState.posts) { post ->
                        PostCard(
                            post = post,
                            isLiked = uiState.likedPostIds.contains(post.id),
                            isSaved = uiState.savedPostIds.contains(post.id),
                            isReposted = uiState.repostedPostIds.contains(post.id),
                            onCommentClick = { postAComentar = post },
                            onLikeClick = { viewModel.toggleLike(post) },
                            onRepostClick = { viewModel.toggleRepost(post) },
                            onSaveClick = { viewModel.toggleSave(post) },
                            onShareClick = { viewModel.sharePost(post) }
                        )
                    }
                }
            }
        }

        if (mostrarComposer) {
            CrearPostScreen(
                initialImageUri = imagenSeleccionada,
                currentUserName = currentUserName,
                currentUserPhotoUrl = currentUser.photoUrl,
                onDismiss = {
                    mostrarComposer = false
                    imagenSeleccionada = null
                },
                onAddPhoto = { galleryLauncher.launch("image/*") },
                onPublicar = { texto, imagenUri ->
                    val post = Post(
                        id = UUID.randomUUID().toString(),
                        userId = currentUser.uid.ifBlank { "demo_user" },
                        userName = currentUserName,
                        userHandle = currentUserHandle,
                        userAvatar = currentUser.photoUrl,
                        mensaje = texto.trim(),
                        ubicacion = "Valencia",
                        tipo = "Avistamiento",
                        fotos = imagenUri?.let { listOf(it) } ?: emptyList(),
                        timestamp = System.currentTimeMillis(),
                        likes = 0,
                        comentarios = 0
                    )

                    viewModel.insertPost(post)
                    mostrarComposer = false
                    imagenSeleccionada = null
                }
            )
        }

        postAComentar?.let { post ->
            CommentSheet(
                post = post,
                onDismiss = { postAComentar = null },
                onSendComment = { texto ->
                    viewModel.addComment(post, texto)
                    postAComentar = null
                }
            )
        }
    }
}

@Composable
private fun CrearPublicacionCard(
    photoUrl: String,
    displayName: String,
    onTextClick: () -> Unit,
    onPhotoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, BlueBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape),
                    color = BluePrimary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = displayName.firstOrNull()?.uppercase() ?: "U",
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTextClick() },
                shape = RoundedCornerShape(24.dp),
                color = CardBackground
            ) {
                Text(
                    text = "¿Qué estás pensando?",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onPhotoClick) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Añadir foto",
                    tint = BluePrimary
                )
            }
        }
    }
}

@Composable
private fun CrearPostScreen(
    initialImageUri: String?,
    currentUserName: String,
    currentUserPhotoUrl: String,
    onDismiss: () -> Unit,
    onAddPhoto: () -> Unit,
    onPublicar: (String, String?) -> Unit
) {
    var texto by rememberSaveable { mutableStateOf("") }
    var imagenUri by rememberSaveable { mutableStateOf(initialImageUri) }

    LaunchedEffect(initialImageUri) {
        imagenUri = initialImageUri
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }

                Text(
                    text = "Crear publicación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = { onPublicar(texto, imagenUri) },
                    enabled = texto.isNotBlank() || imagenUri != null
                ) {
                    Text("PUBLICAR", fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentUserPhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = currentUserPhotoUrl,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape),
                            color = BluePrimary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentUserName.firstOrNull()?.uppercase() ?: "U",
                                    color = BluePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = currentUserName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Visible para la comunidad",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    placeholder = { Text("¿Qué está pasando?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(18.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (imagenUri != null) {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(model = imagenUri),
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = { imagenUri = null },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar imagen",
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedButtonZonaFoto(onClick = onAddPhoto)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onPublicar(texto, imagenUri) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = texto.isNotBlank() || imagenUri != null,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("PUBLICAR")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun OutlinedButtonZonaFoto(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = BluePrimary
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Añadir foto desde la galería",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CommentSheet(
    post: Post,
    onDismiss: () -> Unit,
    onSendComment: (String) -> Unit
) {
    var comment by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Responder a ${post.userName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                placeholder = { Text("Escribe tu comentario") }
            )

            Button(
                onClick = { onSendComment(comment) },
                enabled = comment.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Comentar")
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post,
    isLiked: Boolean,
    isSaved: Boolean,
    isReposted: Boolean,
    onCommentClick: () -> Unit,
    onLikeClick: () -> Unit,
    onRepostClick: () -> Unit,
    onSaveClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, BlueBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (post.userAvatar.isNotBlank()) {
                    AsyncImage(
                        model = post.userAvatar,
                        contentDescription = "Foto de perfil de ${post.userName}",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape),
                        color = BluePrimary.copy(alpha = 0.14f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = post.userName.firstOrNull()?.toString() ?: "?",
                                color = BluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
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

                    if (post.ubicacion.isNotBlank()) {
                        Text(
                            text = post.ubicacion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (post.mensaje.isNotBlank()) {
                Text(
                    text = post.mensaje,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            val images = post.fotos.take(2)
            if (images.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    images.forEach { foto ->
                        AsyncImage(
                            model = foto,
                            contentDescription = "Foto del post",
                            modifier = Modifier
                                .weight(1f)
                                .height(170.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (images.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(color = BlueBorder.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionIcon(
                    icon = Icons.Default.ChatBubbleOutline,
                    text = post.comentarios.toString(),
                    selected = false,
                    onClick = onCommentClick
                )

                ActionIcon(
                    icon = Icons.Default.Repeat,
                    text = if (isReposted) "Publicado" else "Repost",
                    selected = isReposted,
                    onClick = onRepostClick
                )

                ActionIcon(
                    icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    text = post.likes.toString(),
                    selected = isLiked,
                    onClick = onLikeClick
                )

                ActionIcon(
                    icon = Icons.Default.BookmarkBorder,
                    text = if (isSaved) "Guardado" else "Guardar",
                    selected = isSaved,
                    onClick = onSaveClick
                )

                ActionIcon(
                    icon = Icons.Default.Share,
                    text = "Compartir",
                    selected = false,
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${post.userName}: ${post.mensaje}")
                            type = "text/plain"
                        }
                        context.startActivity(
                            Intent.createChooser(sendIntent, "Compartir publicación")
                        )
                        onShareClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun ActionIcon(
    icon: ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) BluePrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .wrapContentHeight()
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1
        )
    }
}