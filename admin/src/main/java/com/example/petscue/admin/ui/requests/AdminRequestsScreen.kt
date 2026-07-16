package com.example.petscue.admin.ui.requests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.petscue.admin.data.model.ProtectoraRequest

private val AdminPrimary = Color(0xFF0D47A1)
private val AdminSurface = Color(0xFFF4F7FC)
private val PendingOrange = Color(0xFFE67E22)
private val PendingBackground = Color(0xFFFFF3E0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRequestsScreen(
    vm: AdminRequestsViewModel,
    onLogout: () -> Unit,
    onOpenDetail: (String) -> Unit
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        containerColor = AdminSurface,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Solicitudes",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "${state.requests.size} pendientes de revisar",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            vm.refresh()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar solicitudes",
                            tint = Color.White
                        )
                    }

                    TextButton(
                        onClick = onLogout
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )

                        Text(
                            text = "Salir",
                            color = Color.White,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AdminPrimary
                )
            )
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                LoadingContent(
                    modifier = Modifier.padding(innerPadding)
                )
            }

            state.errorMessage != null -> {
                ErrorContent(
                    message = state.errorMessage.orEmpty(),
                    modifier = Modifier.padding(innerPadding),
                    onRetry = vm::refresh
                )
            }

            state.requests.isEmpty() -> {
                EmptyRequestsContent(
                    modifier = Modifier.padding(innerPadding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 18.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Pendientes de validación",
                            color = Color(0xFF1D2939),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "Revisa los documentos antes de aceptar o rechazar.",
                            color = Color(0xFF667085),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    items(
                        items = state.requests,
                        key = { request -> request.id }
                    ) { request ->
                        ProtectoraRequestCard(
                            request = request,
                            onClick = {
                                onOpenDetail(request.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProtectoraRequestCard(
    request: ProtectoraRequest,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = AdminPrimary.copy(alpha = 0.12f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = AdminPrimary
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = request.nombre.ifBlank {
                            "Protectora sin nombre"
                        },
                        color = Color(0xFF1D2939),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = request.email.ifBlank {
                            "Email no disponible"
                        },
                        color = Color(0xFF667085),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Abrir solicitud",
                    tint = AdminPrimary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            RequestInfoRow(
                icon = Icons.Default.LocationOn,
                text = listOf(
                    request.ciudad,
                    request.provincia
                ).filter { it.isNotBlank() }
                    .joinToString()
                    .ifBlank { "Ubicación no indicada" }
            )

            Spacer(modifier = Modifier.height(8.dp))

            RequestInfoRow(
                icon = Icons.Default.Description,
                text = "Documentación disponible para revisar"
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge()

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Revisar",
                    color = AdminPrimary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun RequestInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF667085),
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.size(7.dp))

        Text(
            text = text,
            color = Color(0xFF667085),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusBadge() {
    Card(
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = PendingBackground
        )
    ) {
        Text(
            text = "PENDIENTE",
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
            color = PendingOrange,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = AdminPrimary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Cargando solicitudes...",
            color = Color(0xFF667085)
        )
    }
}

@Composable
private fun EmptyRequestsContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .background(
                    color = AdminPrimary.copy(alpha = 0.10f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = AdminPrimary,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Todo al día",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF1D2939),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "No hay solicitudes pendientes de revisión.",
            color = Color(0xFF667085),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No se pudieron cargar las solicitudes",
            color = Color(0xFFB42318),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            color = Color(0xFF667085),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(18.dp))

        TextButton(
            onClick = onRetry
        ) {
            Text(
                text = "Reintentar",
                color = AdminPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}