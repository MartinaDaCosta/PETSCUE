package com.example.petscue.ui.profile.adopta.edit

import android.net.Uri
import com.example.petscue.data.model.Pet

data class EditPhotoItem(
    val remoteUrl: String? = null,
    val localUri: Uri? = null
) {
    val preview: Any
        get() = localUri ?: remoteUrl.orEmpty()
}

data class EditAdoptionPetUiState(
    val petId: String = "",
    val nombre: String = "",
    val especie: String = "",
    val raza: String = "",
    val genero: String = "",
    val edad: String = "",
    val peso: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: String = "",
    val photoItems: List<EditPhotoItem> = emptyList(),
    val originalPet: Pet? = null,
    val isInitialLoading: Boolean = true,
    val isLoading: Boolean = false,
    val isUpdated: Boolean = false,
    val error: String? = null
)