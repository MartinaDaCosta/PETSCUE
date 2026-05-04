package com.example.petscue.ui.mascotas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petscue.data.model.Pet
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarMascotaSheet(
    onDismiss: () -> Unit,
    onPublicar: (Pet) -> Unit
) {
    var nombre    by remember { mutableStateOf("") }
    var especie   by remember { mutableStateOf("Perro") }
    var raza      by remember { mutableStateOf("") }
    var genero    by remember { mutableStateOf("Macho") }
    var edad      by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    val especies = listOf("Perro", "Gato", "Conejo", "Otro")
    val generos  = listOf("Macho", "Hembra")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Publicar mascota perdida",
                style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value         = nombre,
                onValueChange = { nombre = it },
                label         = { Text("Nombre de la mascota") },
                modifier      = Modifier.fillMaxWidth()
            )

            // Selector de especie
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

            OutlinedTextField(
                value         = raza,
                onValueChange = { raza = it },
                label         = { Text("Raza (opcional)") },
                modifier      = Modifier.fillMaxWidth()
            )

            // Selector de género
            Text("Género", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                generos.forEach { op ->
                    FilterChip(
                        selected = genero == op,
                        onClick  = { genero = op },
                        label    = { Text(op) }
                    )
                }
            }

            OutlinedTextField(
                value         = edad,
                onValueChange = { edad = it },
                label         = { Text("Edad aproximada") },
                modifier      = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value         = ubicacion,
                onValueChange = { ubicacion = it },
                label         = { Text("Última ubicación conocida") },
                modifier      = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value         = descripcion,
                onValueChange = { descripcion = it },
                label         = { Text("Descripción") },
                minLines      = 3,
                modifier      = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        onPublicar(
                            Pet(
                                id          = UUID.randomUUID().toString(),
                                nombre      = nombre,
                                especie     = especie,
                                raza        = raza,
                                genero      = genero,
                                edad        = edad,
                                ubicacion   = ubicacion,
                                descripcion = descripcion,
                                estado      = "perdido",
                                timestamp   = System.currentTimeMillis()
                            )
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publicar")
            }
        }
    }
}