package com.example.petscue.ui.mapa.alerts.selectPet

import com.example.petscue.data.model.Pet

data class SelectPetForAlertUiState(
    val pets: List<Pet> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)