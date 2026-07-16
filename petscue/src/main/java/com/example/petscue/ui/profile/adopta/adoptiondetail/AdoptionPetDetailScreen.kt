package com.example.petscue.ui.profile.adopta.adoptiondetail

import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun AdoptionPetDetailScreen(
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onRequestAdoption: (String) -> Unit,
    onPetDeleted: () -> Unit,
    vm: AdoptionPetDetailViewModel = hiltViewModel(),
    auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val state by vm.uiState.collectAsState()

    val showDeleteDialog = remember {
        mutableStateOf(false)
    }

    val fullScreenImage = remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) {
            onPetDeleted()
        }
    }

    if (showDeleteDialog.value) {
        DeleteAdoptionPetDialog(
            onDismiss = {
                showDeleteDialog.value = false
            },
            onConfirm = {
                showDeleteDialog.value = false
                vm.deletePet()
            }
        )
    }

    fullScreenImage.value?.let { imageUrl ->
        FullScreenImageDialog(
            imageUrl = imageUrl,
            onDismiss = {
                fullScreenImage.value = null
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            state.pet == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "No se encontró la mascota",
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                val pet = requireNotNull(state.pet)
                val isOwner = auth.currentUser?.uid == pet.userId
                val photos = pet.fotos

                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = {
                        if (photos.isNotEmpty()) photos.size else 1
                    }
                )

                val scope = rememberCoroutineScope()

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 24.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AdoptionHeader(
                            onBack = onBack
                        )
                    }

                    item {
                        AdoptionHeroCard(
                            petName = pet.nombre,
                            photos = photos,
                            pagerState = pagerState,
                            onPrevious = {
                                scope.launch {
                                    val previous = (
                                            pagerState.currentPage - 1
                                            ).coerceAtLeast(0)

                                    pagerState.animateScrollToPage(
                                        previous
                                    )
                                }
                            },
                            onNext = {
                                scope.launch {
                                    val next = (
                                            pagerState.currentPage + 1
                                            ).coerceAtMost(
                                            photos.lastIndex
                                        )

                                    pagerState.animateScrollToPage(next)
                                }
                            },
                            onImageClick = { selectedImage ->
                                fullScreenImage.value = selectedImage
                            },
                            petNameTitle = pet.nombre,
                            petStatus = pet.estado,
                            petSpecies = pet.especie,
                            petGender = pet.genero,
                            petAge = pet.edad,
                            petWeight = pet.peso
                        )
                    }

                    item {
                        InfoSectionCard(
                            title = "Detalles"
                        ) {
                            DetailRow(
                                label = "Raza",
                                value = pet.raza
                            )

                            DetailRow(
                                label = "Especie",
                                value = pet.especie
                            )

                            DetailRow(
                                label = "Ubicación",
                                value = pet.ubicacion,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )

                            DetailRow(
                                label = "Estado",
                                value = pet.estado
                            )
                        }
                    }

                    item {
                        InfoSectionCard(
                            title = "Descripción"
                        ) {
                            Text(
                                text = pet.descripcion.ifBlank {
                                    "Sin descripción disponible."
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    item {
                        AdoptionActions(
                            isOwner = isOwner,
                            onEdit = {
                                onEditClick(pet.id)
                            },
                            onDelete = {
                                showDeleteDialog.value = true
                            },
                            onRequestAdoption = {
                                onRequestAdoption(pet.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdoptionHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = "Ficha de adopción",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Consulta la información y las fotos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AdoptionHeroCard(
    petName: String,
    photos: List<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onImageClick: (String) -> Unit,
    petNameTitle: String,
    petStatus: String,
    petSpecies: String,
    petGender: String,
    petAge: String,
    petWeight: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            PhotoPager(
                petName = petName,
                photos = photos,
                pagerState = pagerState,
                onPrevious = onPrevious,
                onNext = onNext,
                onImageClick = onImageClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = petNameTitle.ifBlank { "Mascota" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(
                    text = petStatus.ifBlank { "Disponible" }
                )

                if (petSpecies.isNotBlank()) {
                    SoftChip(
                        text = petSpecies
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Género",
                    value = petGender,
                    icon = {
                        Icon(
                            imageVector = if (
                                petGender.equals(
                                    "hembra",
                                    ignoreCase = true
                                )
                            ) {
                                Icons.Default.Female
                            } else {
                                Icons.Default.Male
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Edad",
                    value = petAge,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )

                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Peso",
                    value = petWeight,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MonitorWeight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PhotoPager(
    petName: String,
    photos: List<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onImageClick: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (photos.isEmpty()) {
            Icon(
                imageVector = Icons.Default.Pets,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(58.dp)
            )
            return@Box
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            AsyncImage(
                model = photos[page],
                contentDescription = "Foto de $petName",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onImageClick(photos[page])
                    },
                contentScale = ContentScale.Crop
            )
        }

        if (photos.size > 1) {
            PhotoPagerControls(
                photosCount = photos.size,
                currentPage = pagerState.currentPage,
                onPrevious = onPrevious,
                onNext = onNext
            )
        }
    }
}

@Composable
private fun BoxScope.PhotoPagerControls(
    photosCount: Int,
    currentPage: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    IconButton(
        onClick = onPrevious,
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 10.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.88f
                ),
                shape = CircleShape
            )
            .size(38.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Foto anterior",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    IconButton(
        onClick = onNext,
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .padding(end = 10.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.88f
                ),
                shape = CircleShape
            )
            .size(38.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Foto siguiente",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.rotate(180f)
        )
    }

    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(photosCount) { index ->
            Box(
                modifier = Modifier
                    .size(
                        if (index == currentPage) {
                            9.dp
                        } else {
                            7.dp
                        }
                    )
                    .background(
                        color = if (index == currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.78f
                            )
                        },
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 14.dp,
                    horizontal = 10.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon?.invoke()

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(
                    alpha = 0.88f
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InfoSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()

            if (leadingIcon != null) {
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusChip(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SoftChip(
    text: String
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AdoptionActions(
    isOwner: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRequestAdoption: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isOwner) {
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Editar información",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Eliminar mascota",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                onClick = onRequestAdoption,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Solicitar adopción",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DeleteAdoptionPetDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = "Eliminar mascota",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "¿Seguro que quieres eliminar esta mascota en adopción? " +
                        "Esta acción no se puede deshacer."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Eliminar",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
private fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Imagen ampliada",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(
                            alpha = 0.85f
                        ),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cerrar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}