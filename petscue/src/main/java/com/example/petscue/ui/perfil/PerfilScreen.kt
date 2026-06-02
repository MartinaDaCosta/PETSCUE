package com.example.petscue.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PerfilScreen(onLogout: () -> Unit = {}) {

    val user        = FirebaseAuth.getInstance().currentUser
    val nombre      = user?.displayName?.ifBlank { "Usuario" } ?: "Usuario"
    val email       = user?.email ?: ""
    val iniciales   = nombre.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .ifBlank { "?" }

    var mostrarDialogoLogout by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Spacer(Modifier.height(16.dp))

        // ── Avatar ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = iniciales,
                fontSize   = 36.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // ── Nombre y email ────────────────────────────────────
        Text(
            text       = nombre,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(8.dp))

        // ── Opciones de perfil ────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                PerfilItem(
                    icon  = Icons.Default.Pets,
                    texto = "Mis mascotas publicadas"
                )
                HorizontalDivider()
                PerfilItem(
                    icon  = Icons.Default.Campaign,
                    texto = "Mis novedades"
                )
                HorizontalDivider()
                PerfilItem(
                    icon  = Icons.Default.Notifications,
                    texto = "Notificaciones"
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                PerfilItem(
                    icon  = Icons.Default.Info,
                    texto = "Acerca de Petscue"
                )
                HorizontalDivider()
                PerfilItem(
                    icon  = Icons.Default.Shield,
                    texto = "Política de privacidad"
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Cerrar sesión ─────────────────────────────────────
        OutlinedButton(
            onClick  = { mostrarDialogoLogout = true },
            colors   = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
    }

    // ── Diálogo confirmación logout ───────────────────────────
    if (mostrarDialogoLogout) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoLogout = false },
            icon             = { Icon(Icons.Default.Logout, contentDescription = null) },
            title            = { Text("¿Cerrar sesión?") },
            text             = { Text("Se cerrará tu sesión en este dispositivo.") },
            confirmButton    = {
                TextButton(
                    onClick = {
                        FirebaseAuth.getInstance().signOut()
                        mostrarDialogoLogout = false
                        onLogout()
                    }
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoLogout = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun PerfilItem(icon: ImageVector, texto: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            texto,
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}