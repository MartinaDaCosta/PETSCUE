package com.example.petscue.ui.auth.signup

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
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }
    fun onTelefonoChange(v: String) = _uiState.update { it.copy(telefono = v, errorMessage = null) }
    fun onDireccionChange(v: String) = _uiState.update { it.copy(direccion = v, errorMessage = null) }

    fun onNombreProtectoraChange(v: String) = _uiState.update { it.copy(nombreProtectora = v, errorMessage = null) }
    fun onDescripcionProtectoraChange(v: String) = _uiState.update { it.copy(descripcionProtectora = v, errorMessage = null) }
    fun onProvinciaChange(v: String) = _uiState.update { it.copy(provincia = v, errorMessage = null) }
    fun onCiudadChange(v: String) = _uiState.update { it.copy(ciudad = v, errorMessage = null) }
    fun onWebChange(value: String) { _uiState.update { it.copy(web = value, errorMessage = null) }}

    fun onFacebookChange(value: String) {
        _uiState.update {
            it.copy(facebook = value, errorMessage = null)
        }
    }

    fun onInstagramChange(value: String) {
        _uiState.update {
            it.copy(instagram = value, errorMessage = null)
        }
    }
    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role, errorMessage = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onRegisterClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val s = _uiState.value

            val user = User(
                nombre = s.nombre,
                apellido = s.apellido,
                email = s.email.trim(),
                telefono = s.telefono,
                direccion = s.direccion,
                role = s.selectedRole,
                approvalStatus = if (s.selectedRole == UserRole.PROTECTORA) {
                    ApprovalStatus.PENDING
                } else {
                    ApprovalStatus.APPROVED
                },
                nombreProtectora = s.nombreProtectora,
                descripcionProtectora = s.descripcionProtectora,
                web = s.web,
                facebook = s.facebook,
                instagram = s.instagram,
                provincia = s.provincia,
                ciudad = s.ciudad
            )

            registerUseCase(user, s.password)
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