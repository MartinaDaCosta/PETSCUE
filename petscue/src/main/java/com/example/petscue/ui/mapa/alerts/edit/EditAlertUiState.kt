package com.example.petscue.ui.mapa.alerts.edit

import com.example.petscue.ui.novedades.location.SelectedLocation

data class EditAlertUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,

    val alertId: String = "",
    val petId: String = "",
    val ownerId: String = "",

    val userName: String = "",
    val userPhotoUrl: String = "",

    val nombreMascota: String = "",
    val fotoUrl: String = "",
    val tipoAviso: String = "",
    val sexo: String = "",
    val raza: String = "",
    val edad: String = "",

    val direccionAviso: String = "",
    val selectedLocation: SelectedLocation? = null,
    val radiusMeters: Double = 1500.0,
    val descripcion: String = ""
)