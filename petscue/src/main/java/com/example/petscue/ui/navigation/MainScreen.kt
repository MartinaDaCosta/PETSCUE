package com.example.petscue.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.petscue.ui.novedades.NovedadesScreen
import com.example.petscue.ui.mapa.MapaScreen
import com.example.petscue.ui.mascotas.MascotasScreen
import com.example.petscue.ui.sos.SosScreen
import com.example.petscue.ui.perfil.PerfilScreen

// ── Tabs ──────────────────────────────────────────────────────────────────────
sealed class BottomTab(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Mapa : BottomTab("mapa", Icons.Default.LocationOn, "Mapa")
    object Novedades : BottomTab("novedades", Icons.Default.Campaign, "Novedades")
    object Mascotas : BottomTab("mascotas", Icons.Default.Pets, "Mascotas")
    object Protectoras : BottomTab("protectoras", Icons.Default.Home, "Protectoras")
    object Perfil : BottomTab("perfil", Icons.Default.Person, "Perfil")

    // Esta no va en bottom bar, pero sí la usamos como estado de pantalla actual
    object Sos : BottomTab("sos", Icons.Default.Warning, "SOS")
}

private val tabs = listOf(
    BottomTab.Mapa,
    BottomTab.Novedades,
    BottomTab.Mascotas,
    BottomTab.Protectoras,
    BottomTab.Perfil
)

// ── MainScreen ────────────────────────────────────────────────────────────────
@Composable
fun MainScreen(onLogout: () -> Unit = {}) {
    var currentTab by remember { mutableStateOf<BottomTab>(BottomTab.Novedades) }

    Scaffold(
        topBar = { PetscueTopBar(onLogout = onLogout) },
        bottomBar = {
            PetscueBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { currentTab = BottomTab.Sos },
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Alerta SOS"
                )
            }
        },
        containerColor = Color(0xFFF0F4FF)
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                BottomTab.Mapa -> MapaScreen()
                BottomTab.Novedades -> NovedadesScreen()
                BottomTab.Mascotas -> MascotasScreen()
                BottomTab.Protectoras -> PlaceholderScreen("Protectoras")
                BottomTab.Perfil -> PerfilScreen(onLogout = onLogout)
                BottomTab.Sos -> SosScreen()
            }
        }
    }
}

// ── TopBar ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetscueTopBar(onLogout: () -> Unit = {}) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text          = "PETSCUE 🐾",
                    color         = Color(0xFF1565C0),
                    fontWeight    = FontWeight.ExtraBold,
                    fontSize      = 20.sp,
                    letterSpacing = 1.sp
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notificaciones",
                    tint = Color(0xFF1565C0)
                )
            }
        },
        actions = {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Ajustes",
                    tint = Color(0xFF1565C0)
                )
            }
            DropdownMenu(
                expanded         = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text    = { Text("Cerrar sesión") },
                    onClick = { showMenu = false; onLogout() }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// ── BottomBar ─────────────────────────────────────────────────────────────────
@Composable
fun PetscueBottomBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(8.dp)
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF1565C0),
                    selectedTextColor = Color(0xFF1565C0),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFF1565C0).copy(alpha = 0.12f)
                )
            )
        }
    }
}

// ── Placeholder ───────────────────────────────────────────────────────────────
@Composable
private fun PlaceholderScreen(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF1565C0),
            fontSize   = 18.sp
        )
    }
}