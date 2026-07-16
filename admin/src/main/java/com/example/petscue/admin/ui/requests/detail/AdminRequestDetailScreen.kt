// admin/ui/requests/detail/AdminRequestDetailScreen.kt
package com.example.petscue.admin.ui.requests.detail

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.petscue.admin.data.model.ProtectoraDocument
import com.example.petscue.admin.ui.requests.AdminRequestsViewModel

@Composable
fun AdminRequestDetailScreen(
    vm: AdminRequestsViewModel,
    requestId: String,
    onBack: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    val request = state.requests.find { it.id == requestId }

    var showRejectDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (state.isLoading) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Cargando solicitud...")
        }
        return
    }

    if (request == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Solicitud no encontrada.")

            Spacer(modifier = Modifier.height(12.dp))

            Button(onClick = onBack) {
                Text("Volver")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedButton(
            onClick = onBack
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Solicitud de protectora",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailText("Nombre", request.nombre)
                DetailText("Email", request.email)
                DetailText("Teléfono", request.telefono)
                DetailText("Dirección", request.direccion)
                DetailText("Comunidad", request.comunidad)
                DetailText("Provincia", request.provincia)
                DetailText("Ciudad", request.ciudad)
                DetailText("Estado", request.estado)

                if (request.descripcion.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = request.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        DocumentsSection(
            documents = request.documentos
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                vm.approveRequest(request)
                onBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aprobar solicitud")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                showRejectDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Rechazar solicitud")
        }
    }

    if (showRejectDialog) {
        RejectRequestDialog(
            onDismiss = {
                showRejectDialog = false
            },
            onConfirm = { motivo ->
                vm.rejectRequest(
                    requestId = request.id,
                    motivo = motivo
                )
                showRejectDialog = false
                onBack()
            }
        )
    }
}

@Composable
private fun DetailText(
    label: String,
    value: String
) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun DocumentsSection(
    documents: List<ProtectoraDocument>
) {
    val context = LocalContext.current

    Text(
        text = "Documentación enviada",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (documents.isEmpty()) {
        Text(
            text = "La protectora no ha enviado documentos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        documents.forEachIndexed { index, document ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        openDocument(
                            context = context,
                            url = document.url
                        )
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = document.name.ifBlank {
                                "Documento ${index + 1}"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Text(
                            text = "Pulsa para visualizarlo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    IconButton(
                        onClick = {
                            openDocument(
                                context = context,
                                url = document.url
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Visualizar documento",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            downloadDocument(
                                context = context,
                                url = document.url,
                                fileName = document.name.ifBlank {
                                    "petscue_documento_${index + 1}"
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Descargar documento",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RejectRequestDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var motivo by rememberSaveable {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Rechazar solicitud")
        },
        text = {
            Column {
                Text(
                    text = "Escribe el motivo del rechazo. La protectora podrá verlo y corregir su solicitud."
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = motivo,
                    onValueChange = {
                        motivo = it
                    },
                    label = {
                        Text("Motivo del rechazo")
                    },
                    placeholder = {
                        Text("Ejemplo: El documento adjunto no es legible.")
                    },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(motivo.trim())
                },
                enabled = motivo.trim().isNotBlank()
            ) {
                Text("Rechazar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancelar")
            }
        }
    )
}

private fun openDocument(
    context: Context,
    url: String
) {
    if (url.isBlank()) return

    runCatching {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}

private fun downloadDocument(
    context: Context,
    url: String,
    fileName: String
) {
    if (url.isBlank()) return

    val request = DownloadManager.Request(
        Uri.parse(url)
    )
        .setTitle(fileName)
        .setDescription("Descargando documento")
        .setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        )
        .setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            fileName
        )

    val downloadManager = context.getSystemService(
        Context.DOWNLOAD_SERVICE
    ) as DownloadManager

    downloadManager.enqueue(request)
}