package com.example.petscue.ui.protectoras

import com.example.petscue.data.model.User

data class ProtectorasUiState(
    val query: String = "",
    val allProtectoras: List<User> = emptyList(),
    val filteredProtectoras: List<User> = emptyList(),
    val suggestions: List<String> = emptyList(),

    val nombreSort: NombreSort = NombreSort.A_Z,
    val distanciaSort: DistanciaSort = DistanciaSort.CERCA_LEJOS,

    val selectedComunidad: String? = null,
    val selectedProvincia: String? = null,
    val selectedMunicipio: String? = null,

    val comunidadesDisponibles: List<String> = emptyList(),
    val provinciasDisponibles: List<String> = emptyList(),
    val municipiosDisponibles: List<String> = emptyList(),

    val userLatitude: Double? = null,
    val userLongitude: Double? = null,

    val isLoading: Boolean = false,
    val error: String? = null
)