// ui/admin/AdminApprovalScreen.kt
package com.example.petscue.ui.admin

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.OpenInNew
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.petscue.data.model.ProtectoraDocument
import com.example.petscue.data.model.User

@Composable
fun AdminApprovalScreen(
    vm: AdminApprovalViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    var selectedUserToReject by remember {
        mutableStateOf<User?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Solicitudes de protectoras",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Revisa los datos y documentos antes de aprobar o rechazar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = vm::refresh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Actualizar solicitudes")
        }

        Spacer(modifier = Modifier.height(12.dp))

        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        state.successMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (state.users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay solicitudes pendientes.",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.users,
                    key = { user -> user.uid }
                ) { user ->
                    ProtectoraApprovalCard(
                        user = user,
                        onApprove = {
                            vm.approve(user.uid)
                        },
                        onReject = {
                            selectedUserToReject = user
                        }
                    )
                }
            }
        }
    }

    selectedUserToReject?.let { user ->
        RejectProtectoraDialog(
            protectoraName = user.nombreProtectora.ifBlank {
                user.email
            },
            onDismiss = {
                selectedUserToReject = null
            },
            onConfirm = { reason ->
                vm.reject(
                    uid = user.uid,
                    reason = reason
                )
                selectedUserToReject = null
            }
        )
    }
}

@Composable
private fun ProtectoraApprovalCard(
    user: User,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = user.nombreProtectora.ifBlank {
                    "Protectora sin nombre"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Responsable: ${user.nombre} ${user.apellido}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Email: ${user.email}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Teléfono: ${user.telefono}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Provincia: ${user.provincia}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Ciudad: ${user.ciudad}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (user.descripcionProtectora.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Descripción:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = user.descripcionProtectora,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            DocumentsSection(
                documents = user.documentos,
                documentacionEnviada = user.documentacionEnviada
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onApprove,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Aceptar solicitud")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onReject,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rechazar solicitud")
            }
        }
    }
}

@Composable
private fun DocumentsSection(
    documents: List<ProtectoraDocument>,
    documentacionEnviada: Boolean
) {
    val context = LocalContext.current

    Text(
        text = "Documentación enviada",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    if (!documentacionEnviada || documents.isEmpty()) {
        Text(
            text = "La protectora no ha adjuntado documentos.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    documents.forEachIndexed { index, document ->
        DocumentCard(
            number = index + 1,
            document = document,
            onOpen = {
                openDocument(
                    context = context,
                    url = document.url
                )
            },
            onDownload = {
                downloadDocument(
                    context = context,
                    url = document.url,
                    fileName = document.name.ifBlank {
                        "petscue_documento_${index + 1}"
                    }
                )
            }
        )
    }
}

@Composable
private fun DocumentCard(
    number: Int,
    document: ProtectoraDocument,
    onOpen: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(onClick = onOpen),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = document.name.ifBlank {
                        "Documento $number"
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

            IconButton(onClick = onOpen) {
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = "Visualizar documento",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = "Descargar documento",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RejectProtectoraDialog(
    protectoraName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by rememberSaveable {
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
                    text = "Indica el motivo que verá la protectora \"$protectoraName\"."
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                    },
                    label = {
                        Text("Motivo del rechazo")
                    },
                    placeholder = {
                        Text("Ejemplo: El documento de identificación no es legible.")
                    },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(reason.trim())
                },
                enabled = reason.trim().isNotBlank()
            ) {
                Text("Confirmar rechazo")
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