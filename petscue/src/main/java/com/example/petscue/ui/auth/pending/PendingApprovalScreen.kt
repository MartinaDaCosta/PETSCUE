package com.example.petscue.ui.auth.pending

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petscue.data.model.ProtectoraDocument
import com.example.petscue.ui.theme.AuthCardShape
import com.example.petscue.ui.theme.AuthScreenContainer
import com.example.petscue.ui.theme.AuthTextFieldShape
import com.example.petscue.ui.theme.PetscueError
import com.example.petscue.ui.theme.PetscueSuccess
import com.example.petscue.ui.theme.authFieldColors
import com.example.petscue.ui.theme.authPrimaryButtonColors

@Composable
fun PendingApprovalScreen(
    onApproved: () -> Unit,
    onLogout: () -> Unit = {},
    vm: PendingApprovalViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val context = LocalContext.current

    // Redirige al usuario si la protectora ya ha sido aprobada
    LaunchedEffect(state.isApproved) {
        if (state.isApproved) {
            onApproved()
        }
    }

    // Selector para añadir varios documentos desde el dispositivo
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            vm.onFilesSelected(uris)
        }
    }

    // Abre un documento ya subido usando un intent externo
    fun openDocument(document: ProtectoraDocument) {
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW, document.url.toUri())
            context.startActivity(intent)
        }
    }

    AuthScreenContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Título principal de la pantalla
            Text(
                text = "Cuenta pendiente de validación",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Tu cuenta de protectora ha sido creada, pero aún no ha sido validada por el administrador. Aquí puedes revisar, abrir, eliminar y añadir documentación hasta un máximo de 5 archivos.",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.90f),
                style = MaterialTheme.typography.bodyLarge
            )

            if (state.isRejected) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Solicitud rechazada",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Motivo del administrador:",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = state.rejectionReason.ifBlank {
                            "No se ha indicado un motivo concreto."
                        },
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Corrige la documentación necesaria y vuelve a enviarla. La solicitud volverá a quedar pendiente de revisión.",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Documentación recomendada",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "- CIF o documento identificativo\n- Documento acreditativo de la protectora\n- Información adicional de contacto",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.90f),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Documentos (${state.totalDocuments}/5)",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Botón para seleccionar nuevos documentos
            Button(
                onClick = {
                    if (state.canAddMore) {
                        launcher.launch(arrayOf("application/pdf", "image/*"))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = AuthCardShape,
                colors = authPrimaryButtonColors(),
                enabled = state.canAddMore && !state.isUploading && !state.isDeleting
            ) {
                Text("Añadir documentos")
            }

            if (state.existingDocuments.isNotEmpty()) {
                Text(
                    text = "Ya enviados",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.existingDocuments.forEach { document ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = document.name,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { openDocument(document) }
                            )

                            IconButton(onClick = { openDocument(document) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Abrir documento",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }

                            IconButton(
                                onClick = { vm.deleteExistingDocument(document) },
                                enabled = !state.isDeleting && !state.isUploading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar documento",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }

            if (state.selectedFiles.isNotEmpty()) {
                Text(
                    text = "Pendientes de enviar",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.selectedFiles.forEachIndexed { index, uri ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.10f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = uri.lastPathSegment ?: "Documento ${index + 1}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { vm.removeFile(uri) },
                                enabled = !state.isUploading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Quitar documento",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }

            state.infoMessage?.let {
                Text(
                    text = it,
                    color = PetscueSuccess,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    color = PetscueError,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Botón para enviar la documentación seleccionada
            Button(
                onClick = { vm.submitDocuments() },
                enabled = !state.isUploading && !state.isDeleting && state.selectedFiles.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = AuthCardShape,
                colors = authPrimaryButtonColors()
            ) {
                if (state.isUploading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text("Enviar documentación")
                }
            }

            // Permite cerrar la sesión mientras la cuenta sigue pendiente
            TextButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cerrar sesión",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}