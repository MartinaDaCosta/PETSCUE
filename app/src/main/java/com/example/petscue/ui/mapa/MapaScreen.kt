package com.example.petscue.ui.mapa

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petscue.ui.mascotas.MascotasViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapaScreen(
    viewModel: MascotasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Valencia como centro por defecto
    val valencia = LatLng(39.4699, -0.3763)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(valencia, 12f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = true
            )
        ) {
            // Marcador por cada mascota perdida con coordenadas
            uiState.pets
                .filter { it.latitud != 0.0 && it.longitud != 0.0 }
                .forEach { pet ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(pet.latitud, pet.longitud)
                        ),
                        title = pet.nombre,
                        snippet = "${pet.especie} · ${pet.estado}"
                    )
                }
        }

        // Contador de mascotas en el mapa
        if (uiState.pets.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = "${uiState.pets.size} mascotas",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}