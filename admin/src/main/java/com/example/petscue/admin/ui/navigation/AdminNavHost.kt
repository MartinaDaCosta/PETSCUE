package com.example.petscue.admin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.example.petscue.admin.ui.auth.AdminLoginScreen
import com.example.petscue.admin.ui.requests.AdminRequestsScreen
import com.example.petscue.admin.ui.requests.AdminRequestsViewModel
import com.example.petscue.admin.ui.requests.detail.AdminRequestDetailScreen

@Composable
fun AdminNavHost() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) {
        AdminDestinations.Requests.route
    } else {
        AdminDestinations.Login.route
    }

    val vm = remember { AdminRequestsViewModel() }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AdminDestinations.Login.route) {
            AdminLoginScreen(
                onLoginSuccess = {
                    navController.navigate(AdminDestinations.Requests.route) {
                        popUpTo(AdminDestinations.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AdminDestinations.Requests.route) {
            AdminRequestsScreen(
                vm = vm,
                onOpenDetail = { requestId ->
                    navController.navigate(
                        AdminDestinations.RequestDetail.createRoute(requestId)
                    )
                }
            )
        }

        composable(
            route = AdminDestinations.RequestDetail.route,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId").orEmpty()

            AdminRequestDetailScreen(
                vm = vm,
                requestId = requestId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}