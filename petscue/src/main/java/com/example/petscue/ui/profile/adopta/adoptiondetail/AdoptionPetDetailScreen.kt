package com.example.petscue.ui.profile.adopta.adoptiondetail

import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private val BgColor = Color(0xFFF3F4F6)
private val CardColor = Color(0xFFFFFFFF)
private val AccentBlue = Color(0xFF5B9BF3)
private val AccentBlueDark = Color(0xFF2E6FD8)
private val AccentBlueSoft = Color(0xFFEAF3FF)
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val BorderSoft = Color(0xFFDCE8FF)

@Composable
fun AdoptionPetDetailScreen(
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onRequestAdoption: (String) -> Unit,
    onPetDeleted: () -> Unit,
    vm: AdoptionPetDetailViewModel = hiltViewModel(),
    auth: FirebaseAuth = FirebaseAuth.getInstance()
){
    val state by vm.uiState.collectAsState()
    val showDeleteDialog = remember { mutableStateOf(false) }
    val fullScreenImage = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onPetDeleted()
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = {
                Text(
                    text = "Eliminar mascota",
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text("¿Seguro que quieres eliminar esta mascota en adopción? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog.value = false
                        vm.deletePet()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (fullScreenImage.value != null) {
        Dialog(
            onDismissRequest = { fullScreenImage.value = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { fullScreenImage.value = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fullScreenImage.value,
                    contentDescription = "Imagen ampliada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = { fullScreenImage.value = null },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Cerrar",
                        tint = Color.White
                    )
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgColor
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }

            state.pet == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "No se encontró la mascota",
                        color = TextPrimary
                    )
                }
            }

            else -> {
                val pet = state.pet!!
                val isOwner = auth.currentUser?.uid == pet.userId
                val photos = pet.fotos
                val pagerState = rememberPagerState(
                    initialPage = 0,
                    pageCount = { if (photos.isNotEmpty()) photos.size else 1 }
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = AccentBlue
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = "Ficha de adopción",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = AccentBlue,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Consulta la información y las fotos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6B8DB8)
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = CardColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.15f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(AccentBlueSoft),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (photos.isNotEmpty()) {
                                        HorizontalPager(
                                            state = pagerState,
                                            modifier = Modifier.fillMaxSize()
                                        ) { page ->
                                            AsyncImage(
                                                model = photos[page],
                                                contentDescription = "Foto de ${pet.nombre}",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clickable {
                                                        fullScreenImage.value = photos[page]
                                                    },
                                                contentScale = ContentScale.Crop
                                            )
                                        }

                                        if (photos.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        val previous =
                                                            (pagerState.currentPage - 1).coerceAtLeast(0)
                                                        pagerState.animateScrollToPage(previous)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.CenterStart)
                                                    .padding(start = 10.dp)
                                                    .background(
                                                        Color.White.copy(alpha = 0.82f),
                                                        CircleShape
                                                    )
                                                    .size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Foto anterior",
                                                    tint = AccentBlueDark
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        val next =
                                                            (pagerState.currentPage + 1).coerceAtMost(
                                                                photos.lastIndex
                                                            )
                                                        pagerState.animateScrollToPage(next)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.CenterEnd)
                                                    .padding(end = 10.dp)
                                                    .background(
                                                        Color.White.copy(alpha = 0.82f),
                                                        CircleShape
                                                    )
                                                    .size(38.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Foto siguiente",
                                                    tint = AccentBlueDark,
                                                    modifier = Modifier.rotate(180f)
                                                )
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                photos.forEachIndexed { index, _ ->
                                                    Box(
                                                        modifier = Modifier
                                                            .size(
                                                                if (index == pagerState.currentPage) 9.dp else 7.dp
                                                            )
                                                            .background(
                                                                color = if (index == pagerState.currentPage) {
                                                                    AccentBlue
                                                                } else {
                                                                    Color.White.copy(alpha = 0.75f)
                                                                },
                                                                shape = CircleShape
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Pets,
                                            contentDescription = null,
                                            tint = AccentBlueDark,
                                            modifier = Modifier.size(58.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = pet.nombre.ifBlank { "Mascota" },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusChip(text = pet.estado.ifBlank { "Disponible" })
                                    if (pet.especie.isNotBlank()) {
                                        SoftChip(text = pet.especie)
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
                                        value = pet.genero.ifBlank { "-" },
                                        icon = {
                                            Icon(
                                                imageVector = if (pet.genero.equals("hembra", true)) {
                                                    Icons.Default.Female
                                                } else {
                                                    Icons.Default.Male
                                                },
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )

                                    SummaryCard(
                                        modifier = Modifier.weight(1f),
                                        title = "Edad",
                                        value = pet.edad.ifBlank { "-" },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )

                                    SummaryCard(
                                        modifier = Modifier.weight(1f),
                                        title = "Peso",
                                        value = pet.peso.ifBlank { "-" },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.MonitorWeight,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        InfoSectionCard(
                            title = "Detalles"
                        ) {
                            DetailRow("Raza", pet.raza)
                            DetailRow("Especie", pet.especie)
                            DetailRow(
                                "Ubicación",
                                pet.ubicacion,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            DetailRow("Estado", pet.estado)
                        }
                    }

                    item {
                        InfoSectionCard(
                            title = "Descripción"
                        ) {
                            Text(
                                text = pet.descripcion.ifBlank { "Sin descripción disponible." },
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (state.isOwner) {
                                Button(
                                    onClick = { onEditClick(pet.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentBlue,
                                        contentColor = Color.White
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
                                    onClick = { showDeleteDialog.value = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFCACA)),
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
                                    onClick = { onRequestAdoption(pet.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentBlue,
                                        contentColor = Color.White
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
                }
            }
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
        colors = CardDefaults.cardColors(containerColor = AccentBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon?.invoke()

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
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
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, BorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
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
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(6.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = AccentBlueSoft,
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = AccentBlueDark,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SoftChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFF3F4F6),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = TextSecondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}