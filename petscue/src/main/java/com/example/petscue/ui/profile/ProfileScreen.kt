package com.example.petscue.ui.profile

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.OutlinedButtonDefaults
import coil.compose.AsyncImage
import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.Reply
import com.example.petscue.data.model.UserRole
import com.example.petscue.ui.novedades.PostCard

@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onAddPetClick: () -> Unit,
    onPetClick: (String) -> Unit,
    onAdoptionPetClick: (String) -> Unit,
    onOpenPostDetail: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onEditProfile: () -> Unit = {},
    isOwnProfile: Boolean = true,
    onMessageClick: (String) -> Unit = {},
    profileUpdated: Boolean = false,
    onProfileUpdatedConsumed: () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val user = state.user
    val chatToOpen by vm.openChatEvent.collectAsState()

    LaunchedEffect(profileUpdated) {
        if (profileUpdated) {
            vm.refreshProfile()
            onProfileUpdatedConsumed()
        }
    }

    LaunchedEffect(chatToOpen) {
        val conversationId = chatToOpen ?: return@LaunchedEffect
        onMessageClick(conversationId)
        vm.consumeOpenChatEvent()
    }

    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cargando perfil...",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val isProtectora = user.role == UserRole.PROTECTORA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (!isOwnProfile) {
            OtherProfileTopBar(onBack = onBack)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                if (isProtectora) {
                    ProtectoraProfileHeader(
                        nombreProtectora = user.nombreProtectora,
                        descripcion = user.descripcionProtectora,
                        direccion = user.direccion,
                        telefono = user.telefono,
                        web = user.web,
                        instagram = user.instagram,
                        facebook = user.facebook,
                        photoUrl = user.photoUrl,
                        postsCount = state.posts.size,
                        followersCount = state.followersCount,
                        followingCount = state.followingCount,
                        isOwnProfile = isOwnProfile,
                        isFollowing = state.isFollowing,
                        onEditProfile = onEditProfile,
                        onFollowClick = vm::toggleFollow,
                        onMessageClick = vm::openOrCreateGeneralChat
                    )
                } else {
                    UserProfileHeader(
                        fullName = "${user.nombre} ${user.apellido}".trim(),
                        photoUrl = user.photoUrl,
                        postsCount = state.posts.size,
                        followersCount = state.followersCount,
                        followingCount = state.followingCount,
                        isOwnProfile = isOwnProfile,
                        isFollowing = state.isFollowing,
                        onEditProfile = onEditProfile,
                        onFollowClick = vm::toggleFollow,
                        onMessageClick = vm::openOrCreateGeneralChat
                    )
                }
            }

            item {
                ProfileTabsRow(
                    isProtectora = isProtectora,
                    selectedTab = state.selectedTab,
                    onTabSelected = vm::onTabSelected
                )
            }

            item {
                when (state.selectedTab) {
                    ProfileTab.PETS_OR_ADOPTION -> {
                        if (isProtectora) {
                            AdoptaSection(
                                pets = state.adoptionPets,
                                canAdd = isOwnProfile,
                                onAddPet = onAddPetClick,
                                onPetClick = onAdoptionPetClick
                            )
                        } else {
                            PetsPanel(
                                pets = state.pets,
                                canAdd = isOwnProfile,
                                onAddPet = onAddPetClick,
                                onPetClick = onPetClick
                            )
                        }
                    }

                    ProfileTab.POSTS -> {
                        ProfilePostsPanel(
                            posts = state.posts,
                            viewerUserId = state.currentUserId,
                            onOpenDetail = onOpenPostDetail,
                            onOpenProfile = onOpenProfile,
                            onDeletePost = {},
                            onLikeClick = vm::toggleLike,
                            onRepostClick = vm::toggleRepost,
                            onShareClick = vm::toggleShare
                        )
                    }

                    ProfileTab.REPLIES -> {
                        ProfileRepliesPanel(
                            replies = state.replies,
                            onOpenDetail = onOpenPostDetail,
                            onOpenProfile = onOpenProfile
                        )
                    }

                    ProfileTab.MEDIA -> {
                        ProfilePostsPanel(
                            posts = state.mediaPosts,
                            viewerUserId = state.currentUserId,
                            onOpenDetail = onOpenPostDetail,
                            onOpenProfile = onOpenProfile,
                            onDeletePost = {},
                            onLikeClick = vm::toggleLike,
                            onRepostClick = vm::toggleRepost,
                            onShareClick = vm::toggleShare
                        )
                    }

                    ProfileTab.LIKES -> {
                        ProfilePostsPanel(
                            posts = state.likedPosts,
                            viewerUserId = state.currentUserId,
                            onOpenDetail = onOpenPostDetail,
                            onOpenProfile = onOpenProfile,
                            onDeletePost = {},
                            onLikeClick = vm::toggleLike,
                            onRepostClick = vm::toggleRepost,
                            onShareClick = vm::toggleShare
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileHeader(
    fullName: String,
    photoUrl: String,
    postsCount: Int,
    followersCount: Int,
    followingCount: Int,
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    onEditProfile: () -> Unit,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fullName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = fullName.ifBlank { "Usuario" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    value = postsCount.toString(),
                    label = "Publicaciones"
                )

                ProfileStat(
                    value = followersCount.toString(),
                    label = "Seguidores"
                )

                ProfileStat(
                    value = followingCount.toString(),
                    label = "Seguidos"
                )
            }

            if (isOwnProfile) {
                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Editar perfil")
                }
            } else {
                ProfileActionButtons(
                    isFollowing = isFollowing,
                    onFollowClick = onFollowClick,
                    onMessageClick = onMessageClick
                )
            }
        }
    }
}

@Composable
private fun ProtectoraProfileHeader(
    nombreProtectora: String,
    descripcion: String,
    direccion: String,
    telefono: String,
    web: String,
    instagram: String,
    facebook: String,
    photoUrl: String,
    postsCount: Int,
    followersCount: Int,
    followingCount: Int,
    isOwnProfile: Boolean,
    isFollowing: Boolean,
    onEditProfile: () -> Unit,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de la protectora",
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nombreProtectora.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = nombreProtectora.ifBlank { "Protectora" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-2).dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (telefono.isNotBlank()) {
                        InlineProfileInfo(
                            icon = "📞",
                            value = telefono
                        )
                    }

                    if (direccion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))

                        InlineProfileInfo(
                            icon = "📍",
                            value = direccion
                        )
                    }
                }
            }

            if (descripcion.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = descripcion,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            ProtectoraBottomInfo(
                web = web,
                instagram = instagram,
                facebook = facebook
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    value = postsCount.toString(),
                    label = "Publicaciones"
                )

                ProfileStat(
                    value = followersCount.toString(),
                    label = "Seguidores"
                )

                ProfileStat(
                    value = followingCount.toString(),
                    label = "Seguidos"
                )
            }

            if (isOwnProfile) {
                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Editar perfil")
                }
            } else {
                ProfileActionButtons(
                    isFollowing = isFollowing,
                    onFollowClick = onFollowClick,
                    onMessageClick = onMessageClick
                )
            }
        }
    }
}

@Composable
private fun ProfileActionButtons(
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onFollowClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = if (isFollowing) {
                    "Dejar de seguir"
                } else {
                    "Seguir"
                }
            )
        }

        OutlinedButton(
            onClick = onMessageClick,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("Enviar mensaje")
        }
    }
}

@Composable
private fun ProfileStat(
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun InlineProfileInfo(
    icon: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProtectoraBottomInfo(
    web: String,
    instagram: String,
    facebook: String
) {
    val items = listOfNotNull(
        web.takeIf { it.isNotBlank() }?.let { "🌐" to it },
        instagram.takeIf { it.isNotBlank() }?.let { "📸" to it },
        facebook.takeIf { it.isNotBlank() }?.let { "📘" to it }
    )

    if (items.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (icon, value) ->
            InlineProfileInfo(
                icon = icon,
                value = value
            )
        }
    }
}

@Composable
private fun ProfileTabsRow(
    isProtectora: Boolean,
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit
) {
    val firstTabLabel = if (isProtectora) "Adopta" else "Mascotas"

    val tabs = listOf(
        ProfileTab.PETS_OR_ADOPTION to firstTabLabel,
        ProfileTab.POSTS to "Publicaciones",
        ProfileTab.REPLIES to "Respuestas",
        ProfileTab.MEDIA to "Multimedia",
        ProfileTab.LIKES to "Me gusta"
    )

    val selectedIndex = tabs
        .indexOfFirst { it.first == selectedTab }
        .coerceAtLeast(0)

    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = selectedTab == tab,
                onClick = {
                    onTabSelected(tab)
                },
                text = {
                    Text(
                        text = label,
                        color = if (selectedTab == tab) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (selectedTab == tab) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun PetsPanel(
    pets: List<Pet>,
    canAdd: Boolean,
    onAddPet: () -> Unit,
    onPetClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tus mascotas",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            if (canAdd) {
                TextButton(
                    onClick = onAddPet
                ) {
                    Text(
                        text = "Añadir",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pets.isEmpty()) {
            EmptyPanel(
                message = "Todavía no hay mascotas en este perfil."
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                pets.forEach { pet ->
                    PetHorizontalCard(
                        pet = pet,
                        onClick = onPetClick
                    )
                }
            }
        }
    }
}

@Composable
private fun AdoptaSection(
    pets: List<Pet>,
    canAdd: Boolean,
    onAddPet: () -> Unit,
    onPetClick: (String) -> Unit
) {
    val rows = (pets.size + 1) / 2
    val gridHeight = if (pets.isEmpty()) 0.dp else (rows * 320).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Adopta",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            if (canAdd) {
                TextButton(
                    onClick = onAddPet
                ) {
                    Text(
                        text = "Añadir",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pets.isEmpty()) {
            EmptyPanel(
                message = "No hay animales ahora mismo."
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pets) { pet ->
                    AdoptPetCard(
                        pet = pet,
                        onClick = {
                            onPetClick(pet.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdoptPetCard(
    pet: Pet,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column {
            val petImage = pet.fotos.firstOrNull()

            if (!petImage.isNullOrBlank()) {
                AsyncImage(
                    model = petImage,
                    contentDescription = "Foto de ${pet.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pet.nombre.ifBlank { "Sin nombre" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                AdoptPetInfoLine("Edad", pet.edad)
                AdoptPetInfoLine("Género", pet.genero)
                AdoptPetInfoLine("Especie", pet.especie)
                AdoptPetInfoLine("Raza", pet.raza)
            }
        }
    }
}

@Composable
private fun AdoptPetInfoLine(
    label: String,
    value: String
) {
    Text(
        text = "$label: ${value.ifBlank { "-" }}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun ProfilePostsPanel(
    posts: List<Post>,
    viewerUserId: String,
    onOpenDetail: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    onDeletePost: (Post) -> Unit,
    onLikeClick: (Post) -> Unit,
    onRepostClick: (Post) -> Unit,
    onShareClick: (Post) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        if (posts.isEmpty()) {
            EmptyPanel(
                message = "No hay publicaciones ahora mismo."
            )
        } else {
            posts.forEachIndexed { index, post ->
                PostCard(
                    post = post,
                    isLiked = post.likedBy.contains(viewerUserId),
                    isReposted = post.repostedBy.contains(viewerUserId),
                    isOwner = post.userId == viewerUserId,
                    onDeleteClick = {
                        onDeletePost(post)
                    },
                    onCommentClick = {
                        onOpenDetail(post.id)
                    },
                    onLikeClick = {
                        onLikeClick(post)
                    },
                    onRepostClick = {
                        onRepostClick(post)
                    },
                    onShareClick = {
                        onShareClick(post)
                    },
                    onOpenDetail = {
                        onOpenDetail(post.id)
                    },
                    onOpenProfile = {
                        if (post.userId.isNotBlank()) {
                            onOpenProfile(post.userId)
                        }
                    }
                )

                if (index < posts.lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

        }
    }
}

@Composable
private fun ProfileRepliesPanel(
    replies: List<Reply>,
    onOpenDetail: (String) -> Unit,
    onOpenProfile: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (replies.isEmpty()) {
            EmptyPanel(
                message = "Todavía no has respondido a ninguna publicación."
            )
        } else {
            replies.forEach { reply ->
                ReplyProfileCard(
                    reply = reply,
                    onOpenDetail = onOpenDetail,
                    onOpenProfile = onOpenProfile
                )
            }
        }
    }
}

@Composable
private fun ReplyProfileCard(
    reply: Reply,
    onOpenDetail: (String) -> Unit,
    onOpenProfile: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(
                enabled = reply.postId.isNotBlank()
            ) {
                onOpenDetail(reply.postId)
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (reply.userAvatar.isNotBlank()) {
                    AsyncImage(
                        model = reply.userAvatar,
                        contentDescription = "Avatar de ${reply.userName}",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable(
                                enabled = reply.userId.isNotBlank()
                            ) {
                                onOpenProfile(reply.userId)
                            },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer
                            )
                            .clickable(
                                enabled = reply.userId.isNotBlank()
                            ) {
                                onOpenProfile(reply.userId)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reply.userName
                                .firstOrNull()
                                ?.uppercase()
                                ?: "U",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reply.userName.ifBlank { "Usuario" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (reply.userHandle.isNotBlank()) {
                        Text(
                            text = reply.userHandle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = reply.mensaje,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Ver publicación",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EmptyPanel(
    message: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PetHorizontalCard(
    pet: Pet,
    onClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(pet.id)
            },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val petImage = pet.fotos.firstOrNull()

            if (!petImage.isNullOrBlank()) {
                AsyncImage(
                    model = petImage,
                    contentDescription = "Foto de ${pet.nombre}",
                    modifier = Modifier
                        .size(
                            width = 132.dp,
                            height = 132.dp
                        )
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(
                            width = 132.dp,
                            height = 132.dp
                        )
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = pet.nombre.ifBlank { "Sin nombre" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                PetInfoLine(
                    label = "Género",
                    value = pet.genero.ifBlank { "-" }
                )

                PetInfoLine(
                    label = "Raza",
                    value = pet.raza.ifBlank { "-" }
                )

                PetInfoLine(
                    label = "Edad",
                    value = pet.edad.ifBlank { "-" }
                )

                PetInfoLine(
                    label = "Estado",
                    value = pet.estado.ifBlank { "-" }
                )
            }
        }
    }
}

@Composable
private fun PetInfoLine(
    label: String,
    value: String
) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun OtherProfileTopBar(
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Perfil",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}