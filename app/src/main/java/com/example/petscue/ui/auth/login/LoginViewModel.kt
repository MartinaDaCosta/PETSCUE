package com.example.petscue.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.repository.AuthRepositoryImpl
import com.example.petscue.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repository   = AuthRepositoryImpl()
    private val loginUseCase = LoginUseCase(repository)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errorMessage = null) }

    fun onTogglePasswordVisibility() =
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun onLoginClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loginUseCase(_uiState.value.email, _uiState.value.password)
                .onSuccess {
                    if (repository.isEmailVerified()) {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    } else {
                        repository.logout()
                        _uiState.update {
                            it.copy(isLoading = false,
                                errorMessage = "Verifica tu email antes de continuar.")
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false,
                            errorMessage = e.message ?: "Error al iniciar sesión.")
                    }
                }
        }
    }
}