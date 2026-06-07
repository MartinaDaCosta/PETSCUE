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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.UserRole

private val BluePrimary = Color(0xFF1565C0)
private val BlueDark = Color(0xFF0D47A1)
private val BlueSoft = Color(0xFFEFF4FF)
private val BlueBorder = Color(0xFFB8D3FF)
private val BlueTextSoft = Color(0xFF5E7FAE)

@Composable
fun ProfileScreen(
    onAddPetClick: () -> Unit,
    onPetClick: (String) -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val isProtectora = state.user.role == UserRole.PROTECTORA

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BlueSoft),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ProfileHeader(
                fullName = "${state.user.nombre} ${state.user.apellido}".trim(),
                username = if (state.user.username.isNotBlank()) "@${state.user.username}" else "",
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
                        PetsPanel(
                            pets = state.pets,
                            onAddPet = onAddPetClick,
                            onPetClick = onPetClick
                        )
                    }
                }

                ProfileTab.POSTS -> PostsPanel(state.posts)
                ProfileTab.REPLIES -> PostsPanel(state.replies)
                ProfileTab.MEDIA -> PostsPanel(state.mediaPosts)
                ProfileTab.LIKES -> PostsPanel(state.likedPosts)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueBorder)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = fullName.ifBlank { "Usuario" },
                style = MaterialTheme.typography.headlineSmall,
                color = BlueDark,
                fontWeight = FontWeight.Bold
            )

            if (username.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = BluePrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

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
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    selectedTabIndex = selectedIndex,
                    matchContentSize = true
                ),
                width = Dp.Unspecified,
                color = BluePrimary
            )
        },
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

            TextButton(onClick = onAddPet) {
                Text(
                    text = "Añadir",
                    color = BluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pets.isEmpty()) {
            EmptyPanel("Todavía no has añadido ninguna mascota.")
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
private fun AdoptionPanel(pets: List<Pet>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Animales en adopción",
            style = MaterialTheme.typography.titleLarge,
            color = BlueDark,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
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
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, BlueBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BlueSoft),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = pet.nombre,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = BlueDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
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
private fun PostCard(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(1.dp, BlueBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = post.userName,
                style = MaterialTheme.typography.titleMedium,
                color = BlueDark,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = post.ubicacion,
                style = MaterialTheme.typography.bodySmall,
                color = BluePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = BlueDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "💬 ${post.comentarios}",
                    color = BluePrimary
                )
                Text(
                    text = "❤ ${post.likes}",
                    color = BluePrimary
                )
                if (post.fotos.isNotEmpty()) {
                    Text(
                        text = "📷 Multimedia",
                        color = BlueDark
                    )
                }
            }
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
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = BlueBorder
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
