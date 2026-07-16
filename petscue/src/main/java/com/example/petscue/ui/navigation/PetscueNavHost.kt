package com.example.petscue.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.petscue.data.notifications.saveCurrentFcmToken
import com.example.petscue.ui.admin.AdminApprovalScreen
import com.example.petscue.ui.auth.AuthScreen
import com.example.petscue.ui.auth.login.LoginScreen
import com.example.petscue.ui.auth.pending.PendingApprovalScreen
import com.example.petscue.ui.auth.signup.SignupScreen
import com.example.petscue.ui.mapa.AlertDetailScreen
import com.example.petscue.ui.mapa.MyAlertsScreen
import com.example.petscue.ui.mapa.alerts.create.CreateAlertScreen
import com.example.petscue.ui.mapa.alerts.selectPet.SelectPetForAlertScreen
import com.example.petscue.ui.mensajes.detail.ChatScreen
import com.example.petscue.ui.novedades.NovedadesScreen
import com.example.petscue.ui.novedades.detailpost.PostDetailScreen
import com.example.petscue.ui.onboarding.OnboardingScreen
import com.example.petscue.ui.pet.PetDetailScreen
import com.example.petscue.ui.profile.ProfileScreen
import com.example.petscue.ui.profile.adopta.adoptiondetail.AdoptionPetDetailScreen
import com.example.petscue.ui.profile.adopta.edit.EditAdoptionPetScreen
import com.example.petscue.ui.profile.adopta.request.AdoptionRequestScreen
import com.example.petscue.ui.profile.pet.AddPetScreen
import com.example.petscue.ui.profile.pet.petdetail.EditPetScreen
import com.example.petscue.ui.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PetscueNavHost(
    navController: NavHostController
) {
    val context = LocalContext.current

    val prefs = context.getSharedPreferences(
        "petscue_prefs",
        Context.MODE_PRIVATE
    )

    val isFirstLaunch = prefs.getBoolean(
        "first_launch",
        true
    )

    fun logoutAndNavigateToAuth() {
        FirebaseAuth.getInstance().signOut()

        navController.navigate(Routes.AUTH) {
            popUpTo(0) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen {
                val currentUser = FirebaseAuth.getInstance().currentUser

                when {
                    currentUser == null && isFirstLaunch -> {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.SPLASH) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }

                    currentUser == null -> {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.SPLASH) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }

                    else -> {
                        val uid = currentUser.uid

                        com.google.firebase.firestore.FirebaseFirestore
                            .getInstance()
                            .collection("users")
                            .document(uid)
                            .get()
                            .addOnSuccessListener { document ->
                                val role = document.getString("role").orEmpty()

                                val approvalStatus = document
                                    .getString("approvalStatus")
                                    .orEmpty()

                                val destination = when {
                                    role == "PROTECTORA" &&
                                            approvalStatus == "PENDING" -> {
                                        Routes.PENDING_APPROVAL
                                    }

                                    role == "PROTECTORA" &&
                                            approvalStatus == "REJECTED" -> {
                                        Routes.PENDING_APPROVAL
                                    }

                                    else -> {
                                        Routes.MAIN
                                    }
                                }

                                navController.navigate(destination) {
                                    popUpTo(Routes.SPLASH) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                            .addOnFailureListener {
                                FirebaseAuth.getInstance().signOut()

                                navController.navigate(Routes.AUTH) {
                                    popUpTo(Routes.SPLASH) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                    }
                }
            }
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen {
                prefs.edit()
                    .putBoolean("first_launch", false)
                    .apply()

                navController.navigate(Routes.AUTH) {
                    popUpTo(Routes.ONBOARDING) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }

        composable(Routes.AUTH) {
            AuthScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        launchSingleTop = true
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { destination ->
                    saveCurrentFcmToken()

                    navController.navigate(destination) {
                        popUpTo(Routes.AUTH) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGNUP) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.PENDING_APPROVAL) {
            PendingApprovalScreen(
                onApproved = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.PENDING_APPROVAL) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    logoutAndNavigateToAuth()
                }
            )
        }

        composable(
            route = Routes.MAIN_WITH_TAB,
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.StringType
                    defaultValue = BottomTab.Novedades.route
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments
                ?.getString("tab")
                ?: BottomTab.Novedades.route

            MainScreen(
                navController = navController,
                initialTabRoute = initialTab,
                onLogout = {
                    logoutAndNavigateToAuth()
                }
            )
        }

        composable(Routes.ADD_PET) {
            AddPetScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPetSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("pet_added", true)

                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.PET_DETAIL,
            arguments = listOf(
                navArgument("petId") {
                    type = NavType.StringType
                }
            )
        ) {
            PetDetailScreen(
                onBack = {
                    navController.popBackStack()
                },
                onEditPet = { petId ->
                    navController.navigate(
                        Routes.editPetRoute(petId)
                    )
                },
                onMessageClick = { conversationId ->
                    navController.navigate(
                        Routes.chatDetailRoute(conversationId)
                    )
                },
                onPetDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.EDIT_PET,
            arguments = listOf(
                navArgument("petId") {
                    type = NavType.StringType
                }
            )
        ) {
            EditPetScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPetUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.ADOPTION_DETAIL,
            arguments = listOf(
                navArgument("petId") {
                    type = NavType.StringType
                }
            )
        ) {
            AdoptionPetDetailScreen(
                onBack = {
                    navController.popBackStack()
                },
                onEditClick = { petId ->
                    navController.navigate(
                        Routes.editAdoptionPetRoute(petId)
                    )
                },
                onRequestAdoption = { petId ->
                    navController.navigate(
                        Routes.adoptionRequestRoute(petId)
                    )
                },
                onPetDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.ADOPTION_REQUEST,
            arguments = listOf(
                navArgument("petId") {
                    type = NavType.StringType
                }
            )
        ) {
            AdoptionRequestScreen(
                onBack = {
                    navController.popBackStack()
                },
                onRequestSent = { conversationId ->
                    navController.navigate(
                        Routes.chatDetailRoute(conversationId)
                    ) {
                        popUpTo(Routes.ADOPTION_REQUEST) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(
            route = Routes.EDIT_ADOPTION_PET,
            arguments = listOf(
                navArgument("petId") {
                    type = NavType.StringType
                }
            )
        ) {
            EditAdoptionPetScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPetUpdated = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ADMIN_APPROVAL) {
            AdminApprovalScreen()
        }

        composable("novedades") {
            val currentUserId = FirebaseAuth.getInstance()
                .currentUser
                ?.uid
                .orEmpty()

            NovedadesScreen(
                onOpenDetail = { postId ->
                    navController.navigate(
                        Routes.postDetailRoute(postId)
                    )
                },
                onOpenProfile = { userId ->
                    if (userId == currentUserId) {
                        navController.navigate(
                            Routes.mainRoute(BottomTab.Perfil.route)
                        ) {
                            popUpTo(Routes.MAIN) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(
                            userProfileRoute(userId)
                        )
                    }
                }
            )
        }

        composable(
            route = Routes.POST_DETAIL,
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                }
            )
        ) {
            val currentUserId = FirebaseAuth.getInstance()
                .currentUser
                ?.uid
                .orEmpty()

            PostDetailScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOpenProfile = { userId ->
                    if (userId == currentUserId) {
                        navController.navigate(
                            Routes.mainRoute(BottomTab.Perfil.route)
                        ) {
                            popUpTo(Routes.MAIN) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(
                            userProfileRoute(userId)
                        )
                    }
                }
            )
        }

        composable(Routes.SELECT_PET_FOR_ALERT) { backStackEntry ->
            val petAdded = backStackEntry.savedStateHandle
                .get<Boolean>("pet_added") == true

            SelectPetForAlertScreen(
                onBack = {
                    navController.popBackStack()
                },
                onAddPetClick = {
                    navController.navigate(Routes.ADD_PET)
                },
                onPetSelected = { petId ->
                    navController.navigate(
                        Routes.createAlertRoute(petId)
                    )
                },
                petAdded = petAdded,
                onPetAddedConsumed = {
                    backStackEntry.savedStateHandle["pet_added"] = false
                }
            )
        }

        composable(
            route = Routes.CREATE_ALERT,
            arguments = listOf(
                navArgument("petId") {
                    type = NavType.StringType
                }
            )
        ) {
            CreateAlertScreen(
                onBack = {
                    navController.popBackStack()
                },
                onAlertSaved = {
                    navController.popBackStack(
                        Routes.MAIN,
                        inclusive = false
                    )
                }
            )
        }

        composable(
            route = Routes.CHAT_DETAIL,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.StringType
                }
            )
        ) {
            ChatScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.ALERT_DETAIL,
            arguments = listOf(
                navArgument("petId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments
                ?.getString("petId")
                .orEmpty()

            AlertDetailScreen(
                petId = petId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MY_ALERTS) {
            MyAlertsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onOpenAlert = { petId ->
                    navController.navigate(
                        Routes.alertDetailRoute(petId)
                    )
                }
            )
        }

        composable(
            route = "user_profile/{userId}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                isOwnProfile = false,
                onAddPetClick = {},
                onPetClick = { petId ->
                    navController.navigate(
                        Routes.petDetailRoute(petId)
                    )
                },
                onAdoptionPetClick = { petId ->
                    navController.navigate(
                        Routes.adoptionDetailRoute(petId)
                    )
                },
                onOpenPostDetail = { postId ->
                    navController.navigate(
                        Routes.postDetailRoute(postId)
                    )
                },
                onOpenProfile = { userId ->
                    navController.navigate(
                        userProfileRoute(userId)
                    )
                },
                onMessageClick = {}
            )
        }
    }
}