package com.example.petscue.ui.mapa

import com.example.petscue.data.model.AvisoMapa

data class MapaUiState(
    val alerts: List<AvisoMapa> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val radioNotificaciones: Double = 1500.0,
    val currentUserId: String? = null
)