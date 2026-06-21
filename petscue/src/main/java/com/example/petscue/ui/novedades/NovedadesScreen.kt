@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.petscue.ui.novedades

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.mapSaver
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
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petscue.data.model.Post
import com.example.petscue.ui.novedades.location.LocationPickerScreen
import com.example.petscue.ui.novedades.location.SelectedLocation
import java.util.UUID

private val BluePrimary = Color(0xFF4A90E2)
private val BlueBorder = Color(0xFF6CA9F0)
private val CardBackground = Color(0xFFF7F7F8)
private val DeleteRed = Color(0xFFD32F2F)
private val BlueSoft = Color(0xFFEAF3FF)

private val SelectedLocationSaver: Saver<SelectedLocation, Any> = mapSaver(
    save = { location ->
        mapOf(
            "address" to location.address,
            "lat" to location.lat,
            "lng" to location.lng
        )
    },
    restore = { map ->
        SelectedLocation(
            address = map["address"] as? String ?: "",
            lat = map["lat"] as? Double ?: 39.4699,
            lng = map["lng"] as? Double ?: -0.3763
        )
    }
)

@Composable
fun NovedadesScreen(
    onOpenDetail: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: NovedadesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = uiState.currentUser
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUserName = listOf(currentUser.nombre, currentUser.apellido)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Usuario Petscue" }

    val currentUserHandle = currentUser.username.trim()
        .let { if (it.isBlank()) "@usuario" else "@$it" }

    val context = LocalContext.current

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    var mostrarComposer by rememberSaveable { mutableStateOf(false) }
    var mostrarLocationPicker by rememberSaveable { mutableStateOf(false) }
    var postAComentar by rememberSaveable { mutableStateOf<Post?>(null) }
    var filtroSeleccionado by rememberSaveable { mutableStateOf("Todos") }

    var selectedLocation by rememberSaveable(stateSaver = SelectedLocationSaver) {
        mutableStateOf(SelectedLocation())
    }

    val imagenesSeleccionadas = rememberSaveable(
        saver = listSaver(
            save = { stateList -> stateList.toList() },
            restore = { restored -> mutableStateListOf<String>().apply { addAll(restored) } }
        )
    ) { mutableStateListOf<String>() }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        val nuevasUris = uris.map { it.toString() }.take(4)
        imagenesSeleccionadas.clear()
        imagenesSeleccionadas.addAll(nuevasUris)
        if (nuevasUris.isNotEmpty()) mostrarComposer = true
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    val filtros = listOf(
        "Todos", "Perdido", "Encontrado", "Visto",
        "Urgente", "Adopción", "Acogida", "Recaudación", "Comunidad"
    )

    val postsFiltrados = uiState.posts.filter { post ->
        filtroSeleccionado == "Todos" ||
                post.tipo.equals(filtroSeleccionado, ignoreCase = true)
    }.sortedByDescending { it.timestamp }

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
                                imagenesSeleccionadas.clear()
                                mostrarComposer = true
                            },
                            onPhotoClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        )
                    }

                    item {
                        FiltrosNovedadesSticky(
                            filtros = filtros,
                            filtroSeleccionado = filtroSeleccionado,
                            onFiltroSelected = { filtroSeleccionado = it }
                        )
                    }

                    if (postsFiltrados.isEmpty()) {
                        item {
                            EmptyNovedadesState(filtroSeleccionado = filtroSeleccionado)
                        }
                    } else {
                        items(
                            items = postsFiltrados,
                            key = { post: Post -> post.id }
                        ) { post: Post ->
                            PostCard(
                                post = post,
                                isLiked = uiState.likedPostIds.contains(post.id),
                                isReposted = uiState.repostedPostIds.contains(post.id),
                                isOwner = post.userId == currentUser.uid,
                                onDeleteClick = { viewModel.deletePost(post) },
                                onCommentClick = { onOpenDetail(post.id) },
                                onLikeClick = { viewModel.toggleLike(post) },
                                onRepostClick = { viewModel.toggleRepost(post) },
                                onShareClick = { viewModel.sharePost(post) },
                                onOpenDetail = { onOpenDetail(post.id) },
                                onOpenProfile = {
                                    val targetUserId = post.userId.ifBlank { currentUser.uid }
                                    onOpenProfile(targetUserId)
                                }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        if (mostrarComposer) {
            CrearPostScreen(
                initialImageUris = imagenesSeleccionadas.toList(),
                currentUserName = currentUserName,
                currentUserPhotoUrl = currentUser.photoUrl,
                ubicacionTexto = selectedLocation.address,
                onDismiss = {
                    mostrarComposer = false
                    imagenesSeleccionadas.clear()
                },
                onAddPhotos = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onOpenLocationPicker = { mostrarLocationPicker = true },
                onPublicar = { texto, tipo, fotos ->
                    val post = Post(
                        id = UUID.randomUUID().toString(),
                        userId = currentUser.uid.ifBlank { "demo_user" },
                        userName = currentUserName,
                        userHandle = currentUserHandle,
                        userAvatar = currentUser.photoUrl,
                        mensaje = texto.trim(),
                        ubicacion = selectedLocation.address,
                        tipo = tipo,
                        fotos = emptyList(),
                        timestamp = System.currentTimeMillis(),
                        likes = 0,
                        comentarios = 0
                    )
                    viewModel.insertPost(post = post, localImageUris = fotos.take(4))
                    mostrarComposer = false
                    imagenesSeleccionadas.clear()
                }
            )
        }

        if (mostrarLocationPicker) {
            LocationPickerScreen(
                onDismiss = { mostrarLocationPicker = false },
                onLocationSelected = { location ->
                    selectedLocation = location
                    mostrarLocationPicker = false
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
private fun FiltrosNovedadesSticky(
    filtros: List<String>,
    filtroSeleccionado: String,
    onFiltroSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BlueSoft.copy(alpha = 0.96f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 8.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtros) { filtro ->
                    FilterChip(
                        selected = filtroSeleccionado == filtro,
                        onClick = { onFiltroSelected(filtro) },
                        label = { Text(filtro) },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            1.dp,
                            if (filtroSeleccionado == filtro) BluePrimary.copy(alpha = 0.35f)
                            else BlueBorder.copy(alpha = 0.55f)
                        ),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.White,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNovedadesState(filtroSeleccionado: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (filtroSeleccionado == "Todos") "No hay novedades aún"
            else "No hay publicaciones de tipo $filtroSeleccionado",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (filtroSeleccionado == "Todos") "Sé el primero en publicar una novedad"
            else "Prueba con otro filtro o crea una nueva publicación",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(44.dp).clip(CircleShape),
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

            Spacer(modifier = Modifier.size(10.dp))

            Surface(
                modifier = Modifier.weight(1f).clickable { onTextClick() },
                shape = RoundedCornerShape(24.dp),
                color = CardBackground
            ) {
                Text(
                    text = "¿Qué estás pensando?",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onPhotoClick) {
                Icon(Icons.Default.Image, contentDescription = "Añadir fotos", tint = BluePrimary)
            }
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

    ModalBottomSheet(onDismissRequest = onDismiss) {
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
    isReposted: Boolean,
    isOwner: Boolean,
    onDeleteClick: () -> Unit,
    onCommentClick: () -> Unit,
    onLikeClick: () -> Unit,
    onRepostClick: () -> Unit,
    onShareClick: () -> Unit,
    onOpenDetail: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar publicación") },
            text = { Text("¿Seguro que quieres borrar esta publicación? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDeleteClick() }) {
                    Text("Eliminar", color = DeleteRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, BlueBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
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
                            .clip(CircleShape)
                            .clickable { onOpenProfile() },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { onOpenProfile() },
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenProfile() }
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                    }

                    if (post.ubicacion.isNotBlank()) {
                        Text(
                            text = post.ubicacion,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (isOwner) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar publicación",
                            tint = DeleteRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.clickable { onOpenDetail() }
            ) {
                if (post.mensaje.isNotBlank()) {
                    Text(
                        text = post.mensaje,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                PostImagesGrid(
                    images = post.fotos.take(4),
                    onImageClick = onOpenDetail
                )

                if (post.fotos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        HorizontalDivider(color = BlueBorder.copy(alpha = 0.5f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PostActionItem(
                icon = Icons.Default.ChatBubbleOutline,
                text = post.comentarios.toString(),
                selected = false,
                onClick = onCommentClick
            )
            PostActionItem(
                icon = Icons.Default.Repeat,
                text = if (isReposted) "Publicado" else "Repost",
                selected = isReposted,
                onClick = onRepostClick
            )
            PostActionItem(
                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                text = post.likes.toString(),
                selected = isLiked,
                onClick = onLikeClick
            )
            PostActionItem(
                icon = Icons.Default.Share,
                text = "Compartir",
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

@Composable
private fun PostImagesGrid(
    images: List<String>,
    onImageClick: () -> Unit
) {
    when (images.size) {
        0 -> Unit
        1 -> AsyncImage(
            model = images.first(),
            contentDescription = "Foto del post",
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(14.dp))
                .clickable { onImageClick() },
            contentScale = ContentScale.Crop
        )
        else -> Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            images.chunked(2).forEach { fila ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    fila.forEach { foto ->
                        AsyncImage(
                            model = foto,
                            contentDescription = "Foto del post",
                            modifier = Modifier
                                .weight(1f)
                                .height(170.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageClick() },
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PostActionItem(
    icon: ImageVector,
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val tint = if (selected) BluePrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 6.dp)
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