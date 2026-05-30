package com.example.petscue.ui.mapa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*

private val AzulPrimario   = Color(0xFF1E88E5)
private val AzulSecundario = Color(0xFF42A5F5)
private val Blanco         = Color.White

// Modelo simple de mascota perdida para el mapa
data class MascotaMapa(
    val nombre: String,
    val lat:    Double,
    val lng:    Double,
    val imagen: String = ""
)

@Composable
fun MapaScreen() {
    var vistaActiva by remember { mutableStateOf("MAPA") } // "MAPA" o "LISTA"
    var busqueda    by remember { mutableStateOf("") }

    // Mascotas de ejemplo — en producción vendrán de Firestore
    val mascotas = remember {
        listOf(
            MascotaMapa("Reina", 39.5008, -0.4030),
            MascotaMapa("Rex",   39.4897, -0.4100)
        )
    }

    val camaraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(39.4950, -0.4060), 13f
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F4FF))) {

        // ── BUSCADOR ────────────────────────────────────────────────
        OutlinedTextField(
            value           = busqueda,
            onValueChange   = { busqueda = it },
            placeholder     = { Text("Buscar lugar") },
            leadingIcon     = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine      = true,
            shape           = RoundedCornerShape(24.dp),
            modifier        = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        // ── TOGGLE MAPA / LISTA ──────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AzulPrimario)
                .height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("MAPA", "LISTA").forEach { opcion ->
                    val activo = vistaActiva == opcion
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (activo) Blanco else Color.Transparent)
                            .then(
                                Modifier.padding(horizontal = 4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick  = { vistaActiva = opcion },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text       = opcion,
                                color      = if (activo) AzulPrimario else Blanco,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // ── CONTENIDO PRINCIPAL ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (vistaActiva == "MAPA") {
                // ── MAPA CON MARCADORES ──────────────────────────────
                GoogleMap(
                    modifier           = Modifier.fillMaxSize(),
                    cameraPositionState = camaraState,
                    uiSettings         = MapUiSettings(
                        zoomControlsEnabled    = false,
                        myLocationButtonEnabled = true
                    )
                ) {
                    mascotas.forEach { mascota ->
                        MarkerInfoWindow(
                            state = MarkerState(
                                position = LatLng(mascota.lat, mascota.lng)
                            ),
                            title = mascota.nombre
                        ) {
                            // Ventana personalizada del marcador
                            MarcadorMascotaWindow(mascota)
                        }

                        // Círculo de radio
                        Circle(
                            center      = LatLng(mascota.lat, mascota.lng),
                            radius      = 500.0,
                            fillColor   = AzulPrimario.copy(alpha = 0.15f),
                            strokeColor = AzulPrimario.copy(alpha = 0.4f),
                            strokeWidth = 2f
                        )
                    }
                }
            } else {
                // ── LISTA (puedes reutilizar ProtectorasScreen o una lista de mascotas) ──
                Column(
                    modifier            = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Vista Lista",
                        color      = AzulPrimario,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                    Text(
                        "Aquí irá el listado de mascotas perdidas cerca de ti",
                        color   = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ── Ventana personalizada del marcador ────────────────────────────────────────
@Composable
private fun MarcadorMascotaWindow(mascota: MascotaMapa) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            shape     = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors    = CardDefaults.cardColors(containerColor = Blanco)
        ) {
            Column(
                modifier            = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circular con inicial
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(AzulPrimario.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        mascota.nombre.first().uppercase(),
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = AzulPrimario
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    mascota.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp,
                    color      = Color(0xFF1A1A2E)
                )
            }
        }
        // Pin azul triangular
        Box(
            modifier = Modifier
                .size(width = 12.dp, height = 8.dp)
                .background(AzulPrimario)
        )
    }
}