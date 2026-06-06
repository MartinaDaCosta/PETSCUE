package com.example.petscue.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.UserRole

@Composable
fun ProfileScreen(
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val isProtectora = state.user.role == UserRole.PROTECTORA

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ProfileHeader(
                fullName = "${state.user.nombre} ${state.user.apellido}".trim(),
                username = if (state.user.username.isNotBlank()) {
                    "@${state.user.username}"
                } else {
                    ""
                },
                photoUrl = state.user.photoUrl,
                postsCount = state.posts.size,
                followersCount = state.followersCount,
                followingCount = state.followingCount,
                onEditProfile = { }
            )
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
                        AdoptionPanel(state.adoptionPets)
                    } else {
                        PetsPanel(state.pets)
                    }
                }

                ProfileTab.POSTS -> {
                    PostsPanel(state.posts)
                }

                ProfileTab.REPLIES -> {
                    PostsPanel(state.replies)
                }

                ProfileTab.MEDIA -> {
                    PostsPanel(state.mediaPosts)
                }

                ProfileTab.LIKES -> {
                    PostsPanel(state.likedPosts)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    fullName: String,
    username: String,
    photoUrl: String,
    postsCount: Int,
    followersCount: Int,
    followingCount: Int,
    onEditProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (photoUrl.isNotBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = fullName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(postsCount.toString(), "Publicaciones")
                ProfileStat(followersCount.toString(), "Seguidores")
                ProfileStat(followingCount.toString(), "Seguidos")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = fullName.ifBlank { "Usuario" },
            style = MaterialTheme.typography.titleLarge
        )

        if (username.isNotBlank()) {
            Text(
                text = username,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onEditProfile,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Editar perfil")
        }
    }
}

@Composable
private fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

    PrimaryScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0),
        edgePadding = 0.dp
    ) {
        tabs.forEach { (tab, label) ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                text = { Text(label) }
            )
        }
    }
}

@Composable
private fun PetsPanel(pets: List<Pet>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Tus mascotas",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (pets.isEmpty()) {
            EmptyPanel("Todavía no has añadido mascotas.")
            return
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pets) { pet ->
                PetCard(pet)
            }
        }
    }
}

@Composable
private fun AdoptionPanel(pets: List<Pet>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Animales en adopción",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        if (pets.isEmpty()) {
            EmptyPanel("No hay animales en adopción ahora mismo.")
            return
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp),
            userScrollEnabled = false,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
        ) {
            items(pets.size) { index ->
                val pet = pets[index]
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Text(
                        text = pet.nombre,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PostsPanel(posts: List<Post>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (posts.isEmpty()) {
            EmptyPanel("No hay contenido en este apartado.")
            return
        }

        posts.forEach { post ->
            PostCard(post)
        }
    }
}

@Composable
private fun EmptyPanel(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PetCard(pet: Pet) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = pet.nombre,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${pet.especie} · ${pet.raza}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = pet.estado.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PostCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = post.userName,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = post.ubicacion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.mensaje,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("💬 ${post.comentarios}")
                Text("❤ ${post.likes}")
                if (post.fotos.isNotEmpty()) {
                    Text("📷 Multimedia")
                }
            }
        }
    }
}