package com.example.petscue.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.petscue.ui.mapa.MapaScreen
import com.example.petscue.ui.mensajes.MensajesScreen
import com.example.petscue.ui.notifications.NotificationsScreen
import com.example.petscue.ui.novedades.NovedadesScreen
import com.example.petscue.ui.profile.ProfileScreen
import com.example.petscue.ui.protectoras.ProtectorasScreen

sealed class BottomTab(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Mapa : BottomTab("mapa", Icons.Default.LocationOn, "Mapa")
    object Protectoras : BottomTab("protectoras", Icons.Default.Pets, "Protectoras")
    object Novedades : BottomTab("novedades", Icons.Default.Campaign, "Novedades")
    object Mensajes : BottomTab("mensajes", Icons.AutoMirrored.Filled.Chat, "Mensajes")
    object Perfil : BottomTab("profile", Icons.Default.Person, "Perfil")
}

private val tabs = listOf(
    BottomTab.Mapa,
    BottomTab.Protectoras,
    BottomTab.Novedades,
    BottomTab.Mensajes,
    BottomTab.Perfil
)

@Composable
fun MainScreen(
    navController: NavHostController,
    onLogout: () -> Unit = {}
) {
    var currentTabRoute by rememberSaveable { mutableStateOf(BottomTab.Novedades.route) }
    val currentTab = tabs.firstOrNull { it.route == currentTabRoute } ?: BottomTab.Novedades

    Scaffold(
        topBar = {
            PetscueTopBar(
                onLogout = onLogout,
                onOpenAlert = { petId ->
                    navController.navigate(Routes.alertDetailRoute(petId))
                }
            )
        },
        bottomBar = {
            PetscueBottomBar(
                currentTab = currentTab,
                onTabSelected = { currentTabRoute = it.route }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Routes.SELECT_PET_FOR_ALERT)
                },
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Crear aviso"
                )
            }
        },
        containerColor = Color(0xFFF0F4FF)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentTab) {
                BottomTab.Mapa -> MapaScreen(
                    onOpenAlertDetail = { petId ->
                        navController.navigate(Routes.alertDetailRoute(petId))
                    },
                    onOpenMyAlerts = {
                        navController.navigate(Routes.MY_ALERTS)
                    }
                )

                BottomTab.Novedades -> NovedadesScreen(
                    onOpenDetail = { postId ->
                        navController.navigate(Routes.postDetailRoute(postId))
                    }
                )

                BottomTab.Protectoras -> ProtectorasScreen()

                BottomTab.Mensajes -> MensajesScreen(
                    onConversationClick = { conversationId ->
                        navController.navigate(Routes.chatDetailRoute(conversationId))
                    }
                )

                BottomTab.Perfil -> ProfileScreen(
                    isOwnProfile = true,
                    onAddPetClick = {
                        currentTabRoute = BottomTab.Perfil.route
                        navController.navigate(Routes.ADD_PET)
                    },
                    onPetClick = { petId ->
                        currentTabRoute = BottomTab.Perfil.route
                        navController.navigate(Routes.petDetailRoute(petId))
                    },
                    onAdoptionPetClick = { petId ->
                        currentTabRoute = BottomTab.Perfil.route
                        navController.navigate(Routes.adoptionDetailRoute(petId))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetscueTopBar(
    onLogout: () -> Unit = {},
    onOpenAlert: (String) -> Unit
) {
    var showSettingsMenu by rememberSaveable { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PETSCUE 🐾",
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )
            }
        },
        navigationIcon = {
            NotificationsScreen(
                onOpenAlert = onOpenAlert
            )
        },
        actions = {
            Box {
                IconButton(onClick = { showSettingsMenu = true }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = Color(0xFF1565C0)
                    )
                }

                DropdownMenu(
                    expanded = showSettingsMenu,
                    onDismissRequest = { showSettingsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión") },
                        onClick = {
                            showSettingsMenu = false
                            onLogout()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

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
                        contentDescription = tab.label
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