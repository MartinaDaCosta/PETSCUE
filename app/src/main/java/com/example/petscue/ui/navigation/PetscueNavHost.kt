package com.example.petscue.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.petscue.data.repository.AuthRepositoryImpl
import com.example.petscue.ui.auth.AuthScreen
import com.example.petscue.ui.auth.login.LoginScreen
import com.example.petscue.ui.auth.signup.SignupScreen
import com.example.petscue.ui.onboarding.OnboardingScreen
import com.example.petscue.ui.splash.SplashScreen

@Composable
fun PetscueNavHost(navController: NavHostController) {

    val context       = LocalContext.current
    val prefs         = context.getSharedPreferences("petscue_prefs", Context.MODE_PRIVATE)
    val isFirstLaunch = prefs.getBoolean("first_launch", true)
    val isLoggedIn    = AuthRepositoryImpl().isLoggedIn()

    NavHost(
        navController    = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen {
                when {
                    isLoggedIn    -> navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                    isFirstLaunch -> navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                    else          -> navController.navigate("auth") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        }

        composable("onboarding") {
            OnboardingScreen {
                prefs.edit().putBoolean("first_launch", false).apply()
                navController.navigate("auth") {
                    popUpTo("onboarding") { inclusive = true }
                }
            }
        }

        composable("auth") {
            AuthScreen(
                onNavigateToLogin  = { navController.navigate("login") },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess     = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                onNavigateToSignup = { navController.navigate("signup") }
            )
        }

        composable("signup") {
            SignupScreen(
                onSignupSuccess   = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        composable("main") {
            MainScreen(
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}