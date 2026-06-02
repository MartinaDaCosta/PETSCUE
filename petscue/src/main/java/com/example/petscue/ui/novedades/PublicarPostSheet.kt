package com.example.petscue.ui.novedades

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.petscue.data.model.Post
import com.example.petscue.ui.location.LocationButton
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarPostSheet(
    onDismiss:  () -> Unit,
    onPublicar: (Post) -> Unit
) {
    var mensaje          by remember { mutableStateOf("") }
    var ubicacion        by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf("Avistamiento") }

    val tipos = listOf("Avistamiento", "Adopción", "Encontrado", "Otro")

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
            Text("Nueva novedad",
                style = MaterialTheme.typography.titleLarge)

            Text("Tipo", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tipos.forEach { tipo ->
                    FilterChip(
                        selected = tipoSeleccionado == tipo,
                        onClick  = { tipoSeleccionado = tipo },
                        label    = { Text(tipo) }
                    )
                }
            }

            OutlinedTextField(
                value         = mensaje,
                onValueChange = { mensaje = it },
                label         = { Text("Describe la novedad") },
                minLines      = 4,
                modifier      = Modifier.fillMaxWidth()
            )

            // Ubicación con GPS
            LocationButton(
                ubicacion            = ubicacion,
                onUbicacionDetectada = { texto, _, _ ->
                    ubicacion = texto
                }
            )

            Button(
                enabled  = mensaje.isNotBlank(),
                onClick  = {
                    onPublicar(
                        Post(
                            id        = UUID.randomUUID().toString(),
                            userName  = "Yo",
                            mensaje   = mensaje,
                            ubicacion = ubicacion,
                            tipo      = tipoSeleccionado,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publicar")
            }
        }
    }
}