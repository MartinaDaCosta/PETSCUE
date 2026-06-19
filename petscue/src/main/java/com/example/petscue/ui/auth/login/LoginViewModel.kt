package com.example.petscue.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.repository.AuthRepository
import com.example.petscue.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // Estado mutable interno del ViewModel
    private val _uiState = MutableStateFlow(LoginUiState())

    // Estado público solo lectura para la UI
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Actualiza el email y limpia mensajes anteriores
    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    // Actualiza la contraseña y limpia mensajes anteriores
    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    // Alterna entre mostrar u ocultar la contraseña
    fun onTogglePasswordVisibility() {
        _uiState.update {
            it.copy(passwordVisible = !it.passwordVisible)
        }
    }

    // Muestra el diálogo de recuperación de contraseña
    fun showForgotPasswordDialog() {
        _uiState.update {
            it.copy(
                showForgotPasswordDialog = true,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    // Oculta el diálogo de recuperación
    fun hideForgotPasswordDialog() {
        _uiState.update {
            it.copy(showForgotPasswordDialog = false)
        }
    }

    // Inicia sesión y carga el perfil del usuario
    fun onLoginClick() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            val email = _uiState.value.email.trim()
            val password = _uiState.value.password

            loginUseCase(email, password)
                .onSuccess {
                    if (!repository.isEmailVerified()) {
                        repository.logout()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = false,
                                errorMessage = "Verifica tu email antes de continuar.",
                                successMessage = null
                            )
                        }
                        return@onSuccess
                    }

                    repository.getCurrentUserProfile()
                        .onSuccess { user ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    errorMessage = null,
                                    successMessage = null,
                                    userRole = user.role,
                                    approvalStatus = user.approvalStatus
                                )
                            }
                        }
                        .onFailure { e ->
                            repository.logout()
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    isSuccess = false,
                                    errorMessage = e.message
                                        ?: "No se pudo cargar el perfil del usuario.",
                                    successMessage = null
                                )
                            }
                        }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = e.message ?: "Error al iniciar sesión.",
                            successMessage = null
                        )
                    }
                }
        }
    }

    // Envía el correo de recuperación de contraseña
    fun onForgotPasswordConfirm() {
        val email = _uiState.value.email.trim()

        if (email.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Introduce tu correo para recuperar la contraseña.",
                    successMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            repository.resetPassword(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showForgotPasswordDialog = false,
                            errorMessage = null,
                            successMessage = "Te hemos enviado un correo para restablecer la contraseña."
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                                ?: "No se pudo enviar el correo de recuperación.",
                            successMessage = null
                        )
                    }
                }
        }
    }
}