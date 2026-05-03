package com.example.petscue.ui.auth.login

data class LoginUiState(
    val email:           String  = "",
    val password:        String  = "",
    val passwordVisible: Boolean = false,
    val isLoading:       Boolean = false,
    val isSuccess:       Boolean = false,
    val errorMessage:    String? = null
)