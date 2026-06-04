package com.example.petscue.admin.ui.navigation

sealed class AdminDestinations(val route: String) {
    object Login : AdminDestinations("login")
    object Requests : AdminDestinations("requests")
    object RequestDetail : AdminDestinations("request_detail/{requestId}") {
        fun createRoute(requestId: String) = "request_detail/$requestId"
    }
}