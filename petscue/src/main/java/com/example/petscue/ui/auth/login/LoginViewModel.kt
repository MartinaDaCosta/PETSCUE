package com.example.petscue.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.domain.AuthRepository
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

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value, errorMessage = null, successMessage = null)
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(password = value, errorMessage = null, successMessage = null)
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update {
            it.copy(passwordVisible = !it.passwordVisible)
        }
    }

    fun showForgotPasswordDialog() {
        _uiState.update {
            it.copy(showForgotPasswordDialog = true, errorMessage = null, successMessage = null)
        }
    }

    fun hideForgotPasswordDialog() {
        _uiState.update {
            it.copy(showForgotPasswordDialog = false)
        }
    }

    fun onLoginClick() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            loginUseCase(_uiState.value.email, _uiState.value.password)
                .onSuccess {
                    if (!repository.isEmailVerified()) {
                        repository.logout()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Verifica tu email antes de continuar."
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
                                    errorMessage = e.message
                                        ?: "No se pudo cargar el perfil del usuario."
                                )
                            }
                        }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Error al iniciar sesión."
                        )
                    }
                }
        }
    }

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
                it.copy(isLoading = true, errorMessage = null, successMessage = null)
            }

            repository.resetPassword(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showForgotPasswordDialog = false,
                            successMessage = "Te hemos enviado un correo para restablecer la contraseña."
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message
                                ?: "No se pudo enviar el correo de recuperación."
                        )
                    }
                }
        }
    }
}