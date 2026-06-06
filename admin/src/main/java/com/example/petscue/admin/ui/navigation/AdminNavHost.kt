package com.example.petscue.admin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.petscue.admin.ui.auth.AdminGateViewModel
import com.example.petscue.admin.ui.auth.AdminLoadingScreen
import com.example.petscue.admin.ui.auth.AdminLoginScreen
import com.example.petscue.admin.ui.auth.AdminSessionState
import com.example.petscue.admin.ui.requests.AdminRequestsScreen
import com.example.petscue.admin.ui.requests.AdminRequestsViewModel
import com.example.petscue.admin.ui.requests.detail.AdminRequestDetailScreen

@Composable
fun AdminNavHost() {
    val navController = rememberNavController()
    val gateVm: AdminGateViewModel = viewModel()
    val sessionState by gateVm.sessionState.collectAsState()

    val requestsVm = remember { AdminRequestsViewModel() }

    when (sessionState) {
        is AdminSessionState.Loading -> {
            AdminLoadingScreen()
        }

        is AdminSessionState.LoggedOut,
        is AdminSessionState.Error -> {
            val message = (sessionState as AdminSessionState.Error).message
            AdminLoginScreen(
                startupError = message,
                onLoginSuccess = { gateVm.checkSession() }
            )
        }

        is AdminSessionState.Authorized -> {
            NavHost(
                navController = navController,
                startDestination = AdminDestinations.Requests.route
            ) {
                composable(AdminDestinations.Requests.route) {
                    AdminRequestsScreen(
                        vm = requestsVm,
                        onLogout = {
                            gateVm.logout()
                        },
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
                        vm = requestsVm,
                        requestId = requestId,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}