package com.example.petscue.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

private val AzulPrimario   = Color(0xFF1E88E5)
private val AzulSecundario = Color(0xFF42A5F5)
private val Blanco         = Color.White

data class BottomNavItem(
    val ruta:  String,
    val icono: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("mapa",        Icons.Filled.LocationOn,   "Mapa"),
    BottomNavItem("campana",     Icons.Filled.Notifications,"Alertas"),
    BottomNavItem("sos",         Icons.Filled.Warning,      "SOS"),
    BottomNavItem("protectoras", Icons.Filled.Favorite,     "Protectoras"),
    BottomNavItem("perfil",      Icons.Filled.Person,       "Perfil")
)

@Composable
fun PetscueBottomBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val rutaActual = backStack?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(AzulPrimario, AzulSecundario))
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                if (item.ruta == "sos") {
                    // Botón SOS central grande
                    IconButton(
                        onClick = { navController.navigate(item.ruta) },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Blanco)
                    ) {
                        Text(
                            text  = "SOS",
                            color = AzulPrimario,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                        )
                    }
                } else {
                    val seleccionado = rutaActual == item.ruta
                    IconButton(
                        onClick = {
                            if (!seleccionado) {
                                navController.navigate(item.ruta) {
                                    popUpTo("mapa") { saveState = true }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector        = item.icono,
                            contentDescription = item.label,
                            tint = if (seleccionado) Blanco
                            else Blanco.copy(alpha = 0.55f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}