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

    // Estado mutable interno del formulario
    private val _uiState = MutableStateFlow(SignupUiState())

    // Estado público observado desde la UI
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    // Cambios en datos personales
    fun onNombreChange(v: String) = _uiState.update { it.copy(nombre = v, errorMessage = null) }
    fun onApellidoChange(v: String) = _uiState.update { it.copy(apellido = v, errorMessage = null) }
    fun onUsernameChange(v: String) = _uiState.update { it.copy(username = v, errorMessage = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }
    fun onTelefonoChange(v: String) = _uiState.update { it.copy(telefono = v, errorMessage = null) }


    // Foto de perfil seleccionada
    fun onProfileImageSelected(uri: Uri?) {
        _uiState.update {
            it.copy(
                selectedImageUri = uri,
                errorMessage = null
            )
        }
    }

    // Cambios en datos de protectora
    fun onNombreProtectoraChange(v: String) = _uiState.update { it.copy(nombreProtectora = v, errorMessage = null) }
    fun onDescripcionProtectoraChange(v: String) = _uiState.update { it.copy(descripcionProtectora = v, errorMessage = null) }
    fun onProvinciaChange(v: String) = _uiState.update { it.copy(provincia = v, errorMessage = null) }
    fun onCiudadChange(v: String) = _uiState.update { it.copy(ciudad = v, errorMessage = null) }
    fun onWebChange(v: String) = _uiState.update { it.copy(web = v, errorMessage = null) }
    fun onFacebookChange(v: String) = _uiState.update { it.copy(facebook = v, errorMessage = null) }
    fun onInstagramChange(v: String) = _uiState.update { it.copy(instagram = v, errorMessage = null) }
    fun onRoleSelected(role: UserRole) = _uiState.update { it.copy(selectedRole = role, errorMessage = null) }
    fun onAcceptedPrivacyPolicyChange(value: Boolean) = _uiState.update { it.copy(acceptedPrivacyPolicy = value) }
    // Alterna la visibilidad de la contraseña
    fun onTogglePasswordVisibility() {
        _uiState.update {
            it.copy(passwordVisible = !it.passwordVisible)
        }
    }

    // Añade documentos de verificación sin duplicados
    fun onVerificationDocumentsSelected(uris: List<Uri>) {
        _uiState.update { current ->
            current.copy(
                verificationDocuments = (current.verificationDocuments + uris).distinct(),
                errorMessage = null
            )
        }
    }

    // Elimina un documento de verificación seleccionado
    fun removeVerificationDocument(uri: Uri) {
        _uiState.update { current ->
            current.copy(
                verificationDocuments = current.verificationDocuments.filterNot { it == uri }
            )
        }
    }

    // Actualiza las notas adicionales de validación
    fun onVerificationNotesChange(value: String) {
        _uiState.update {
            it.copy(
                verificationNotes = value,
                errorMessage = null
            )
        }
    }

    // Actualiza el texto libre de la búsqueda de dirección
    fun onAddressQueryChange(value: String) {
        _uiState.update {
            it.copy(
                addressQuery = value,
                errorMessage = null
            )
        }
    }

    // Guarda las sugerencias cargadas desde la búsqueda
    fun onAddressSuggestionsLoaded(items: List<AddressSuggestion>) {
        _uiState.update {
            it.copy(addressSuggestions = items)
        }
    }

    // Aplica una sugerencia elegida por el usuario
    fun onAddressSuggestionSelected(item: AddressSuggestion) {
        _uiState.update {
            it.copy(
                direccion = item.fullAddress,
                addressQuery = item.fullAddress,
                addressSuggestions = emptyList(),
                latitude = item.latitude,
                longitude = item.longitude,
                errorMessage = null
            )
        }
    }

    fun onResolvedLocationData(
        direccion: String,
        provincia: String,
        ciudad: String,
        lat: Double?,
        lng: Double?
    ) {
        _uiState.update {
            it.copy(
                direccion = direccion,
                addressQuery = direccion,
                provincia = provincia,
                ciudad = ciudad,
                latitude = lat,
                longitude = lng,
                addressSuggestions = emptyList(),
                errorMessage = null
            )
        }
    }

    // Valida y envía el registro al caso de uso
    fun onRegisterClick() {
        viewModelScope.launch {
            val s = _uiState.value
            val direccionFinal = s.direccion.trim()

            if (
                s.nombre.trim().isBlank() ||
                s.apellido.trim().isBlank() ||
                s.email.trim().isBlank() ||
                s.password.isBlank() ||
                s.username.trim().isBlank()
            ) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Completa nombre, apellido, nombre de usuario, email y contraseña."
                    )
                }
                return@launch
            }

            if (s.selectedRole == UserRole.PROTECTORA) {
                if (s.nombreProtectora.trim().isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Completa el nombre de la protectora."
                        )
                    }
                    return@launch
                }

                if (direccionFinal.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Completa la ubicación de la protectora."
                        )
                    }
                    return@launch
                }

                if (s.verificationDocuments.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Adjunta al menos un documento de verificación de la protectora."
                        )
                    }
                    return@launch
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val user = User(
                nombre = s.nombre.trim(),
                apellido = s.apellido.trim(),
                username = s.username.trim().lowercase(),
                email = s.email.trim(),
                telefono = s.telefono.trim(),
                direccion = direccionFinal,
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
                ciudad = s.ciudad.trim(),
                latitude = s.latitude ?: 0.0,
                longitude = s.longitude ?: 0.0,
                motivoRevision = s.verificationNotes.trim()
            )

            registerUseCase(
                user = user,
                password = s.password,
                profileImageUri = s.selectedImageUri,
                verificationDocuments = s.verificationDocuments
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            }.onFailure { e ->
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