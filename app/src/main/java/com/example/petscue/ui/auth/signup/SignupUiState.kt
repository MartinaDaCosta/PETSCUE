package com.example.petscue.ui.auth.signup

data class SignupUiState(
    val nombre:          String  = "",
    val apellido:        String  = "",
    val email:           String  = "",
    val password:        String  = "",
    val telefono:        String  = "",
    val direccion:       String  = "",
    val passwordVisible: Boolean = false,
    val isLoading:       Boolean = false,
    val isSuccess:       Boolean = false,
    val errorMessage:    String? = null
)