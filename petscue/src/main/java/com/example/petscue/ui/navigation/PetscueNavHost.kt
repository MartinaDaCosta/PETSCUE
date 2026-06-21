package com.example.petscue.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.petscue.notifications.saveCurrentFcmToken
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
import com.example.petscue.ui.profile.adoption.AdoptionPetDetailScreen
import com.example.petscue.ui.profile.pet.AddPetScreen
import com.example.petscue.ui.profile.pet.editpet.EditAdoptionPetScreen
import com.example.petscue.ui.profile.pet.petdetail.EditPetScreen
import com.example.petscue.ui.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun PetscueNavHost(
    navController: NavHostController
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("petscue_prefs", Context.MODE_PRIVATE)
    val isFirstLaunch = prefs.getBoolean("first_launch", true)

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen {
                val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

                when {
                    isLoggedIn -> {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }

                    isFirstLaunch -> {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }

                    else -> {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                }
            }
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen {
                prefs.edit().putBoolean("first_launch", false).apply()
                navController.navigate(Routes.AUTH) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            }
        }

        composable(Routes.AUTH) {
            AuthScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN)
                },
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { destination ->
                    saveCurrentFcmToken()
                    navController.navigate(destination) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Routes.SIGNUP)
                }
            )
        }

        composable(Routes.SIGNUP) {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SIGNUP) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        composable(Routes.PENDING_APPROVAL) {
            PendingApprovalScreen(
                onApproved = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.PENDING_APPROVAL) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.PENDING_APPROVAL) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                navController = navController,
                onLogout = {
                    navController.navigate(Routes.AUTH) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
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
                    navController.navigate(Routes.editPetRoute(petId))
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
                    navController.navigate(Routes.editAdoptionPetRoute(petId))
                },
                onPetDeleted = {
                    navController.popBackStack()
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
            NovedadesScreen(
                onOpenDetail = { postId ->
                    navController.navigate("post_detail/$postId")
                }
            )
        }

        composable(Routes.POST_DETAIL) {
            PostDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SELECT_PET_FOR_ALERT) { backStackEntry ->
            val petAdded = backStackEntry.savedStateHandle.get<Boolean>("pet_added") == true

            SelectPetForAlertScreen(
                onBack = { navController.popBackStack() },
                onAddPetClick = { navController.navigate(Routes.ADD_PET) },
                onPetSelected = { petId ->
                    navController.navigate(Routes.createAlertRoute(petId))
                },
                petAdded = petAdded,
                onPetAddedConsumed = {
                    backStackEntry.savedStateHandle["pet_added"] = false
                }
            )
        }

        composable(Routes.CREATE_ALERT) {
            CreateAlertScreen(
                onBack = { navController.popBackStack() },
                onAlertSaved = {
                    navController.popBackStack(Routes.MAIN, inclusive = false)
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
                onBack = { navController.popBackStack() }
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
            val petId = backStackEntry.arguments?.getString("petId").orEmpty()

            AlertDetailScreen(
                petId = petId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MY_ALERTS) {
            MyAlertsScreen(
                onBack = { navController.popBackStack() },
                onOpenAlert = { petId ->
                    navController.navigate(Routes.alertDetailRoute(petId))
                }
            )
        }
    }
}