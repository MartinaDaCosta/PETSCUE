package com.example.petscue.admin.ui.auth

sealed interface AdminSessionState {
    object Loading : AdminSessionState
    object LoggedOut : AdminSessionState
    object Authorized : AdminSessionState
    data class Error(val message: String) : AdminSessionState
}