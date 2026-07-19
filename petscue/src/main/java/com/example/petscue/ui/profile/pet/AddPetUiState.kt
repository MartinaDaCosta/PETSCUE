package com.example.petscue.ui.profile.pet

import android.net.Uri

data class AddPetUiState(
    val nombre: String = "",
    val especie: String = "",
    val raza: String = "",
    val genero: String = "",
    val edad: String = "",
    val peso: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: String = "en casa",
    val photoUris: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)