package com.example.petscue.ui.mascotas

import com.example.petscue.data.model.Pet

data class MascotasUiState(
    val pets: List<Pet> = emptyList(),
    val filtroEstado: String = "perdido",
    val isLoading: Boolean = false,
    val error: String? = null,
    val filtroActivo: String = "perdido"
)