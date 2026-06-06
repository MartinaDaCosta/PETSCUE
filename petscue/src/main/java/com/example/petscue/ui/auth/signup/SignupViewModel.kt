package com.example.petscue.ui.auth.signup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.ApprovalStatus
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.example.petscue.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onNombreChange(v: String) = _uiState.update { it.copy(nombre = v, errorMessage = null) }
    fun onApellidoChange(v: String) = _uiState.update { it.copy(apellido = v, errorMessage = null) }
    fun onUsernameChange(v: String) = _uiState.update { it.copy(username = v, errorMessage = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }
    fun onTelefonoChange(v: String) = _uiState.update { it.copy(telefono = v, errorMessage = null) }
    fun onDireccionChange(v: String) = _uiState.update { it.copy(direccion = v, errorMessage = null) }
    fun onProfileImageSelected(uri: Uri?) = _uiState.update { it.copy(selectedImageUri = uri, errorMessage = null) }

    fun onNombreProtectoraChange(v: String) = _uiState.update { it.copy(nombreProtectora = v, errorMessage = null) }
    fun onDescripcionProtectoraChange(v: String) = _uiState.update { it.copy(descripcionProtectora = v, errorMessage = null) }
    fun onProvinciaChange(v: String) = _uiState.update { it.copy(provincia = v, errorMessage = null) }
    fun onCiudadChange(v: String) = _uiState.update { it.copy(ciudad = v, errorMessage = null) }
    fun onWebChange(value: String) = _uiState.update { it.copy(web = value, errorMessage = null) }
    fun onFacebookChange(value: String) = _uiState.update { it.copy(facebook = value, errorMessage = null) }
    fun onInstagramChange(value: String) = _uiState.update { it.copy(instagram = value, errorMessage = null) }
    fun onRoleSelected(role: UserRole) = _uiState.update { it.copy(selectedRole = role, errorMessage = null) }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onRegisterClick() {
        viewModelScope.launch {
            val s = _uiState.value

            if (s.nombre.isBlank() || s.apellido.isBlank() || s.email.isBlank() || s.password.isBlank() || s.username.isBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Completa nombre, apellido, nombre de usuario, email y contraseña."
                    )
                }
                return@launch
            }

            if (s.selectedRole == UserRole.PROTECTORA &&
                (s.nombreProtectora.isBlank() || s.provincia.isBlank() || s.ciudad.isBlank())
            ) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Completa los campos obligatorios de la protectora."
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val user = User(
                nombre = s.nombre.trim(),
                apellido = s.apellido.trim(),
                username = s.username.trim().lowercase(),
                email = s.email.trim(),
                telefono = s.telefono.trim(),
                direccion = s.direccion.trim(),
                role = s.selectedRole,
                approvalStatus = if (s.selectedRole == UserRole.PROTECTORA) {
                    ApprovalStatus.PENDING
                } else {
                    ApprovalStatus.APPROVED
                },
                nombreProtectora = s.nombreProtectora.trim(),
                descripcionProtectora = s.descripcionProtectora.trim(),
                web = s.web.trim(),
                facebook = s.facebook.trim(),
                instagram = s.instagram.trim(),
                provincia = s.provincia.trim(),
                ciudad = s.ciudad.trim()
            )

            registerUseCase(user, s.password, s.selectedImageUri)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Error al crear la cuenta."
                        )
                    }
                }
        }
    }
}