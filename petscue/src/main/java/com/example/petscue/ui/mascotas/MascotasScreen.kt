package com.example.petscue.ui.mascotas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MascotasScreen(
    viewModel: MascotasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filtros  = listOf("perdido", "encontrado", "adoptado")
    var mostrarSheet by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir mascota")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ── Filtros ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filtros.forEach { filtro ->
                    FilterChip(
                        selected = uiState.filtroActivo == filtro,
                        onClick  = { viewModel.setFiltro(filtro) },
                        label    = { Text(filtro.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // ── Lista ────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.pets.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No hay mascotas ${uiState.filtroActivo}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding       = PaddingValues(16.dp),
                        verticalArrangement  = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.pets) { pet ->
                            PetCard(pet = pet)
                        }
                    }
                }
            }
        }

        // ── Bottom Sheet ─────────────────────────────────────────
        if (mostrarSheet) {
            PublicarMascotaSheet(
                onDismiss  = { mostrarSheet = false },
                onPublicar = { pet ->
                    viewModel.insertPet(pet)
                    mostrarSheet = false
                }
            )
        }
    }
}


// ── PetCard ──────────────────────────────────────────────────────────────────
@Composable
fun PetCard(pet: com.example.petscue.data.model.Pet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text  = pet.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text  = "${pet.especie} · ${pet.raza.ifBlank { "Raza desconocida" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (pet.ubicacion.isNotBlank()) {
                Text(
                    text  = "📍 ${pet.ubicacion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (pet.descripcion.isNotBlank()) {
                Text(
                    text  = pet.descripcion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


