package com.example.petscue.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.User
import com.example.petscue.data.repository.AuthRepositoryImpl
import com.example.petscue.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {

    private val registerUseCase = RegisterUseCase(AuthRepositoryImpl())

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onNombreChange(v: String)    = _uiState.update { it.copy(nombre    = v, errorMessage = null) }
    fun onApellidoChange(v: String)  = _uiState.update { it.copy(apellido  = v, errorMessage = null) }
    fun onEmailChange(v: String)     = _uiState.update { it.copy(email     = v, errorMessage = null) }
    fun onPasswordChange(v: String)  = _uiState.update { it.copy(password  = v, errorMessage = null) }
    fun onTelefonoChange(v: String)  = _uiState.update { it.copy(telefono  = v, errorMessage = null) }
    fun onDireccionChange(v: String) = _uiState.update { it.copy(direccion = v, errorMessage = null) }
    fun onTogglePasswordVisibility() =
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun onRegisterClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val s = _uiState.value
            val user = User(
                nombre    = s.nombre,
                apellido  = s.apellido,
                email     = s.email.trim(),
                telefono  = s.telefono,
                direccion = s.direccion
            )
            registerUseCase(user, s.password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false,
                            errorMessage = e.message ?: "Error al crear la cuenta.")
                    }
                }
        }
    }
}