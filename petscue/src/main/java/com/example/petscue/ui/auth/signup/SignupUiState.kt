package com.example.petscue.ui.auth.signup

import android.net.Uri
import com.example.petscue.data.model.UserRole

// Sugerencia de dirección obtenida desde búsqueda o geolocalización
data class AddressSuggestion(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val fullAddress: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

// Estado completo de la pantalla de registro
data class SignupUiState(
    // Datos personales básicos
    val nombre: String = "",
    val apellido: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val telefono: String = "",
    val direccion: String = "",

    // Rol y foto de perfil
    val selectedRole: UserRole = UserRole.USER,
    val selectedImageUri: Uri? = null,

    // Datos extra para protectoras
    val nombreProtectora: String = "",
    val descripcionProtectora: String = "",
    val web: String = "",
    val facebook: String = "",
    val instagram: String = "",
    val provincia: String = "",
    val ciudad: String = "",

    // Documentación de verificación
    val verificationDocuments: List<Uri> = emptyList(),
    val verificationNotes: String = "",

    // Estado de dirección y coordenadas
    val addressQuery: String = "",
    val addressSuggestions: List<AddressSuggestion> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Estado visual de la pantalla
    val passwordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val acceptedPrivacyPolicy: Boolean = false
)