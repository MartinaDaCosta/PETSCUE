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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
import com.example.petscue.ui.theme.PetscueBlue

sealed class BottomTab(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Mapa : BottomTab(
        route = "mapa",
        icon = Icons.Default.LocationOn,
        label = "Mapa"
    )

    data object Protectoras : BottomTab(
        route = "protectoras",
        icon = Icons.Default.Pets,
        label = "Protectoras"
    )

    data object Novedades : BottomTab(
        route = "novedades",
        icon = Icons.Default.Campaign,
        label = "Novedades"
    )

    data object Mensajes : BottomTab(
        route = "mensajes",
        icon = Icons.AutoMirrored.Filled.Chat,
        label = "Mensajes"
    )

    data object Perfil : BottomTab(
        route = "profile",
        icon = Icons.Default.Person,
        label = "Perfil"
    )
}

private val tabs = listOf(
    BottomTab.Mapa,
    BottomTab.Protectoras,
    BottomTab.Novedades,
    BottomTab.Mensajes,
    BottomTab.Perfil
)

fun userProfileRoute(userId: String): String {
    return "user_profile/$userId"
}

@Composable
fun MainScreen(
    navController: NavHostController,
    initialTabRoute: String = BottomTab.Novedades.route,
    onLogout: () -> Unit = {}
) {
    var currentTabRoute by rememberSaveable {
        mutableStateOf(initialTabRoute)
    }

    val currentTab = tabs.firstOrNull {
        it.route == currentTabRoute
    } ?: BottomTab.Novedades

    val backStackEntry = navController.currentBackStackEntry

    val profileUpdated by backStackEntry
        ?.savedStateHandle
        ?.getStateFlow("profile_updated", false)
        ?.collectAsState()
        ?: rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PetscueTopBar(
                onLogout = onLogout,
                onOpenAlert = { petId ->
                    navController.navigate(
                        Routes.alertDetailRoute(petId)
                    )
                }
            )
        },
        bottomBar = {
            PetscueBottomBar(
                currentTab = currentTab,
                onTabSelected = { selectedTab ->
                    currentTabRoute = selectedTab.route
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(
                        Routes.SELECT_PET_FOR_ALERT
                    )
                },
                containerColor = PetscueBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Crear aviso"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                BottomTab.Mapa -> {
                    MapaScreen(
                        onOpenAlertDetail = { petId ->
                            navController.navigate(
                                Routes.alertDetailRoute(petId)
                            )
                        },
                        onOpenMyAlerts = {
                            navController.navigate(
                                Routes.MY_ALERTS
                            )
                        }
                    )
                }

                BottomTab.Protectoras -> {
                    ProtectorasScreen(
                        onProtectoraClick = { protectoraId ->
                            navController.navigate(
                                userProfileRoute(protectoraId)
                            )
                        }
                    )
                }

                BottomTab.Novedades -> {
                    NovedadesScreen(
                        onOpenDetail = { postId ->
                            navController.navigate(
                                Routes.postDetailRoute(postId)
                            )
                        },
                        onOpenProfile = { userId ->
                            val currentUid = com.google.firebase.auth.FirebaseAuth
                                .getInstance()
                                .currentUser
                                ?.uid

                            if (userId == currentUid) {
                                currentTabRoute = BottomTab.Perfil.route
                            } else {
                                navController.navigate(
                                    userProfileRoute(userId)
                                )
                            }
                        }
                    )
                }

                BottomTab.Mensajes -> {
                    MensajesScreen(
                        onConversationClick = { conversationId ->
                            navController.navigate(
                                Routes.chatDetailRoute(conversationId)
                            )
                        }
                    )
                }

                BottomTab.Perfil -> {
                    ProfileScreen(
                        isOwnProfile = true,
                        onAddPetClick = {
                            navController.navigate(Routes.ADD_PET)
                        },
                        onPetClick = { petId ->
                            navController.navigate(Routes.petDetailRoute(petId))
                        },
                        onAdoptionPetClick = { petId ->
                            navController.navigate(Routes.adoptionDetailRoute(petId))
                        },
                        onOpenPostDetail = { postId ->
                            navController.navigate(Routes.postDetailRoute(postId))
                        },
                        onOpenProfile = { userId ->
                            navController.navigate(userProfileRoute(userId))
                        },
                        onEditProfile = {
                            navController.navigate(Routes.EDIT_PROFILE)
                        },
                        onMessageClick = {},
                        profileUpdated = profileUpdated,
                        onProfileUpdatedConsumed = {
                            backStackEntry
                                ?.savedStateHandle
                                ?.set("profile_updated", false)
                        }
                    )
                }
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
    var showSettingsMenu by rememberSaveable {
        mutableStateOf(false)
    }

    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PETSCUE 🐾",
                    color = PetscueBlue,
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
                IconButton(
                    onClick = {
                        showSettingsMenu = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = PetscueBlue
                    )
                }

                DropdownMenu(
                    expanded = showSettingsMenu,
                    onDismissRequest = {
                        showSettingsMenu = false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Cerrar sesión")
                        },
                        onClick = {
                            showSettingsMenu = false
                            onLogout()
                        }
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun PetscueBottomBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.shadow(8.dp)
    ) {
        tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = {
                    onTabSelected(tab)
                },
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
                    selectedIconColor = PetscueBlue,
                    selectedTextColor = PetscueBlue,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = PetscueBlue.copy(alpha = 0.12f)
                )
            )
        }
    }
}