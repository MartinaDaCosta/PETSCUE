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
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.UserRole
import com.example.petscue.ui.novedades.PostCard

private val BluePrimary = Color(0xFF1565C0)
private val BlueDark = Color(0xFF0D47A1)
private val BlueSoft = Color(0xFFEFF4FF)
private val BlueBorder = Color(0xFFB8D3FF)
private val BlueTextSoft = Color(0xFF5E7FAE)

@Composable
fun ProfileScreen(
    onAddPetClick: () -> Unit,
    onPetClick: (String) -> Unit,
    onAdoptionPetClick: (String) -> Unit,
    onOpenPostDetail: (String) -> Unit,
    onOpenProfile: (String) -> Unit,
    isOwnProfile: Boolean = true,
    onMessageClick: (String) -> Unit = {},
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val user = state.user
    val chatToOpen by vm.openChatEvent.collectAsState()

    if (user == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BlueSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Cargando perfil...",
                color = BlueDark,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    val isProtectora = user.role == UserRole.PROTECTORA
    val viewerUserId = state.currentUserId

    LaunchedEffect(chatToOpen) {
        val conversationId = chatToOpen ?: return@LaunchedEffect
        onMessageClick(conversationId)
        vm.consumeOpenChatEvent()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueSoft),
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
                    onEditProfile = { },
                    onFollowClick = vm::toggleFollow,
                    onMessageClick = { vm.openOrCreateGeneralChat() }
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
                    onEditProfile = { },
                    onFollowClick = vm::toggleFollow,
                    onMessageClick = { vm.openOrCreateGeneralChat() }
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

                ProfileTab.POSTS -> ProfilePostsPanel(
                    posts = state.posts,
                    viewerUserId = viewerUserId,
                    onOpenDetail = onOpenPostDetail,
                    onOpenProfile = onOpenProfile,
                    onDeletePost = { }
                )

                ProfileTab.REPLIES -> ProfilePostsPanel(
                    posts = state.replies,
                    viewerUserId = viewerUserId,
                    onOpenDetail = onOpenPostDetail,
                    onOpenProfile = onOpenProfile,
                    onDeletePost = { }
                )

                ProfileTab.MEDIA -> ProfilePostsPanel(
                    posts = state.mediaPosts.filter { it.fotos.isNotEmpty() },
                    viewerUserId = viewerUserId,
                    onOpenDetail = onOpenPostDetail,
                    onOpenProfile = onOpenProfile,
                    onDeletePost = { }
                )

                ProfileTab.LIKES -> ProfilePostsPanel(
                    posts = state.likedPosts,
                    viewerUserId = viewerUserId,
                    onOpenDetail = onOpenPostDetail,
                    onOpenProfile = onOpenProfile,
                    onDeletePost = { }
                )
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                            .background(BluePrimary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = fullName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fullName.ifBlank { "Usuario" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = BlueDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = BlueBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(postsCount.toString(), "Publicaciones")
                ProfileStat(followersCount.toString(), "Seguidores")
                ProfileStat(followingCount.toString(), "Seguidos")
            }

            if (isOwnProfile) {
                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Editar perfil")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (isFollowing) "Dejar de seguir" else "Seguir")
                    }

                    OutlinedButton(
                        onClick = onMessageClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Enviar mensaje")
                    }
                }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueBorder)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
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
                            .background(BluePrimary.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = nombreProtectora.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = BluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreProtectora.ifBlank { "Protectora" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = BlueDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-2).dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (telefono.isNotBlank()) {
                        InlineProfileInfo("📞", telefono)
                    }

                    if (direccion.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        InlineProfileInfo("📍", direccion)
                    }
                }
            }

            if (descripcion.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BlueSoft
                ) {
                    Text(
                        text = descripcion,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = BlueDark
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
                color = BlueBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(postsCount.toString(), "Publicaciones")
                ProfileStat(followersCount.toString(), "Seguidores")
                ProfileStat(followingCount.toString(), "Seguidos")
            }

            if (isOwnProfile) {
                Button(
                    onClick = onEditProfile,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BluePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Editar perfil")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onFollowClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (isFollowing) "Dejar de seguir" else "Seguir")
                    }

                    OutlinedButton(
                        onClick = onMessageClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Enviar mensaje")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = BluePrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = BlueTextSoft,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun InlineProfileInfo(
    icon: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = BlueDark,
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)

    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp,
        containerColor = Color.White,
        contentColor = BluePrimary,
        divider = {
            HorizontalDivider(
                thickness = 1.dp,
                color = BlueBorder
            )
        }
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = {
                    Text(
                        text = label,
                        color = if (selectedTab == tab) BluePrimary else BlueTextSoft,
                        fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium
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
                color = BlueDark,
                fontWeight = FontWeight.Bold
            )

            if (canAdd) {
                TextButton(onClick = onAddPet) {
                    Text(
                        text = "Añadir",
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pets.isEmpty()) {
            EmptyPanel("Todavía no hay mascotas en este perfil.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                color = BlueDark,
                fontWeight = FontWeight.Bold
            )

            if (canAdd) {
                TextButton(onClick = onAddPet) {
                    Text(
                        text = "Añadir",
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pets.isEmpty()) {
            EmptyPanel("No hay animales ahora mismo.")
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
                        onClick = { onPetClick(pet.id) }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueBorder)
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
                        .background(BlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = BluePrimary,
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
                    color = BlueDark,
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
        color = BluePrimary,
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
    onDeletePost: (Post) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (posts.isEmpty()) {
            EmptyPanel("No hay posts ahora mismo.")
            return
        }
        }

        posts.forEach { post ->
            PostCard(
                post = post,
                isLiked = false,
                isReposted = false,
                isOwner = post.userId == viewerUserId,
                onDeleteClick = { onDeletePost(post) },
                onCommentClick = { onOpenDetail(post.id) },
                onLikeClick = { },
                onRepostClick = { },
                onShareClick = { },
                onOpenDetail = { onOpenDetail(post.id) },
                onOpenProfile = {
                    val targetUserId = post.userId.ifBlank { viewerUserId }
                    if (targetUserId.isNotBlank()) {
                        onOpenProfile(targetUserId)
                    }
                }
            )
        }
    }


@Composable
private fun EmptyPanel(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                color = BlueTextSoft,
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
            .clickable { onClick(pet.id) },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueBorder)
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
                        .size(width = 132.dp, height = 132.dp)
                        .clip(RoundedCornerShape(22.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(width = 132.dp, height = 132.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(BlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = BluePrimary,
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
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold
                )

                PetInfoLine("Género", pet.genero.ifBlank { "-" })
                PetInfoLine("Raza", pet.raza.ifBlank { "-" })
                PetInfoLine("Edad", pet.edad.ifBlank { "-" })
                PetInfoLine("Estado", pet.estado.ifBlank { "-" })
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
        color = BluePrimary
    )
}