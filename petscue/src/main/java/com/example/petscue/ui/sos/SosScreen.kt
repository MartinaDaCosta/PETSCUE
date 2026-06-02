package com.example.petscue.ui.sos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petscue.data.model.Pet
import com.example.petscue.ui.mascotas.MascotasViewModel
import java.util.UUID

@Composable
fun SosScreen(
    viewModel: MascotasViewModel = hiltViewModel()
) {
    var nombre      by remember { mutableStateOf("") }
    var especie     by remember { mutableStateOf("Perro") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion   by remember { mutableStateOf("") }
    var publicado   by remember { mutableStateOf(false) }

    val especies = listOf("Perro", "Gato", "Conejo", "Otro")

    if (publicado) {
        // ── Pantalla de confirmación ───────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🆘", fontSize = 72.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "¡Alerta SOS enviada!",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "La comunidad ha sido notificada.\nEsperamos que encuentres a ${nombre.ifBlank { "tu mascota" }} pronto. 🐾",
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color     = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = { publicado = false }) {
                Text("Publicar otro SOS")
            }
        }
        return
    }

    // ── Formulario SOS ─────────────────────────────────────────
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Cabecera
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        "Alerta SOS",
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.error,
                        style      = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Notifica a toda la comunidad al instante",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Nombre
        OutlinedTextField(
            value         = nombre,
            onValueChange = { nombre = it },
            label         = { Text("Nombre de la mascota") },
            modifier      = Modifier.fillMaxWidth()
        )

        // Especie
        Text("Especie", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            especies.forEach { op ->
                FilterChip(
                    selected = especie == op,
                    onClick  = { especie = op },
                    label    = { Text(op) }
                )
            }
        }

        // Descripción
        OutlinedTextField(
            value         = descripcion,
            onValueChange = { descripcion = it },
            label         = { Text("Descripción (color, tamaño, señas)") },
            minLines      = 3,
            modifier      = Modifier.fillMaxWidth()
        )

        // Ubicación
        OutlinedTextField(
            value         = ubicacion,
            onValueChange = { ubicacion = it },
            label         = { Text("Última ubicación conocida") },
            leadingIcon   = {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Botón SOS
        Button(
            onClick = {
                if (nombre.isNotBlank()) {
                    viewModel.insertPet(
                        Pet(
                            id          = UUID.randomUUID().toString(),
                            nombre      = nombre,
                            especie     = especie,
                            raza        = "",
                            genero      = "",
                            edad        = "",
                            descripcion = descripcion,
                            ubicacion   = ubicacion,
                            estado      = "perdido",
                            timestamp   = System.currentTimeMillis()
                        )
                    )
                    publicado = true
                }
            },
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                "ENVIAR ALERTA SOS",
                fontWeight = FontWeight.Bold,
                fontSize   = 16.sp
            )
        }
    }
}