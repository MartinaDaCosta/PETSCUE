package com.example.petscue.ui.auth.login

import com.example.petscue.data.model.ApprovalStatus
import com.example.petscue.data.model.UserRole

// Estado completo de la pantalla de inicio de sesión
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val showForgotPasswordDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val userRole: UserRole? = null,
    val approvalStatus: ApprovalStatus? = null
)