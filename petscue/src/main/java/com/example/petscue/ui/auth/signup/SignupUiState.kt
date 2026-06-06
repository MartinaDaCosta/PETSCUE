package com.example.petscue.ui.auth.signup

import android.net.Uri
import com.example.petscue.data.model.UserRole

data class SignupUiState(
    val nombre: String = "",
    val apellido: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val selectedRole: UserRole = UserRole.USER,
    val selectedImageUri: Uri? = null,
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