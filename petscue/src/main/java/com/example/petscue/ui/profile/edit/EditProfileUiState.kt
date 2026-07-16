package com.example.petscue.ui.profile.edit

import android.net.Uri
import com.example.petscue.data.model.UserRole

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val success: Boolean = false,

    val role: UserRole? = null,

    val nombre: String = "",
    val apellido: String = "",

    val nombreProtectora: String = "",
    val descripcionProtectora: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val web: String = "",
    val instagram: String = "",
    val facebook: String = "",

    val currentPhotoUrl: String = "",
    val selectedPhotoUri: Uri? = null,

    val errorMessage: String? = null
)