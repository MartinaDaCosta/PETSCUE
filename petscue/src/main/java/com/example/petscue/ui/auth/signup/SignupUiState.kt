package com.example.petscue.ui.auth.signup

import com.example.petscue.data.model.UserRole

data class SignupUiState(
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val password: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val selectedRole: UserRole = UserRole.USER,
    val nombreProtectora: String = "",
    val descripcionProtectora: String = "",
    val web: String = "",
    val facebook: String = "",
    val instagram: String = "",
    val provincia: String = "",
    val ciudad: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)