package com.example.petscue.ui.mapa.alerts.create

import com.example.petscue.data.model.Pet
import com.example.petscue.ui.novedades.location.SelectedLocation

data class CreateAlertUiState(
    val petId: String = "",
    val pet: Pet? = null,
    val alertType: AlertType = AlertType.LOST,
    val selectedLocation: SelectedLocation? = null,
    val radiusMeters: Double = 1500.0,
    val descripcion: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)