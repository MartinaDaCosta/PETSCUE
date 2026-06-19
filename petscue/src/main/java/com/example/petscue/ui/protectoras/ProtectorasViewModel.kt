package com.example.petscue.ui.protectoras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.User
import com.example.petscue.data.repository.ProtectorasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class ProtectorasViewModel @Inject constructor(
    private val repository: ProtectorasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProtectorasUiState(isLoading = true))
    val uiState: StateFlow<ProtectorasUiState> = _uiState.asStateFlow()

    init {
        loadProtectoras()
    }

    private fun loadProtectoras() {
        viewModelScope.launch {
            runCatching { repository.getProtectoras() }
                .onSuccess { protectoras ->
                    _uiState.update {
                        it.copy(
                            allProtectoras = protectoras,
                            isLoading = false,
                            error = null,
                            comunidadesDisponibles = protectoras
                                .map { user -> user.comunidad.trim() }
                                .filter { value -> value.isNotBlank() }
                                .distinct()
                                .sorted()
                        )
                    }
                    refreshLocationFilters()
                    updateSuggestions()
                    applyFilters()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al cargar protectoras"
                        )
                    }
                }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(query = query) }
        updateSuggestions()
        applyFilters()
    }

    fun onSuggestionSelected(suggestion: String) {
        _uiState.update {
            it.copy(
                query = suggestion,
                suggestions = emptyList()
            )
        }
        applyFilters()
    }

    fun onNombreSortChanged(sort: NombreSort) {
        _uiState.update { it.copy(nombreSort = sort) }
        applyFilters()
    }

    fun onDistanciaSortChanged(sort: DistanciaSort) {
        _uiState.update { it.copy(distanciaSort = sort) }
        applyFilters()
    }

    fun onComunidadChanged(comunidad: String?) {
        _uiState.update {
            it.copy(
                selectedComunidad = comunidad,
                selectedProvincia = null,
                selectedMunicipio = null
            )
        }
        refreshLocationFilters()
        applyFilters()
    }

    fun onProvinciaChanged(provincia: String?) {
        _uiState.update {
            it.copy(
                selectedProvincia = provincia,
                selectedMunicipio = null
            )
        }
        refreshLocationFilters()
        applyFilters()
    }

    fun onMunicipioChanged(municipio: String?) {
        _uiState.update { it.copy(selectedMunicipio = municipio) }
        applyFilters()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                query = "",
                suggestions = emptyList(),
                nombreSort = NombreSort.A_Z,
                distanciaSort = DistanciaSort.CERCA_LEJOS,
                selectedComunidad = null,
                selectedProvincia = null,
                selectedMunicipio = null
            )
        }
        refreshLocationFilters()
        applyFilters()
    }

    fun setUserLocation(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(
                userLatitude = latitude,
                userLongitude = longitude
            )
        }
        applyFilters()
    }

    private fun updateSuggestions() {
        val state = _uiState.value

        if (state.query.isBlank()) {
            _uiState.update { it.copy(suggestions = emptyList()) }
            return
        }

        val suggestions = state.allProtectoras
            .flatMap { listOf(it.nombreProtectora, it.username) }
            .filter { it.isNotBlank() && it.contains(state.query, ignoreCase = true) }
            .distinct()
            .take(5)

        _uiState.update { it.copy(suggestions = suggestions) }
    }

    private fun refreshLocationFilters() {
        val state = _uiState.value

        val provincias = state.allProtectoras
            .asSequence()
            .filter { user ->
                state.selectedComunidad.isNullOrBlank() ||
                        user.comunidad.equals(state.selectedComunidad, ignoreCase = true)
            }
            .map { it.provincia.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .toList()

        val municipios = state.allProtectoras
            .asSequence()
            .filter { user ->
                (state.selectedComunidad.isNullOrBlank() ||
                        user.comunidad.equals(state.selectedComunidad, ignoreCase = true)) &&
                        (state.selectedProvincia.isNullOrBlank() ||
                                user.provincia.equals(state.selectedProvincia, ignoreCase = true))
            }
            .map { it.ciudad.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .toList()

        _uiState.update {
            it.copy(
                provinciasDisponibles = provincias,
                municipiosDisponibles = municipios
            )
        }
    }

    private fun applyFilters() {
        val state = _uiState.value

        var filtered = state.allProtectoras.asSequence()

        if (state.query.isNotBlank()) {
            filtered = filtered.filter { user ->
                user.nombreProtectora.contains(state.query, ignoreCase = true) ||
                        user.username.contains(state.query, ignoreCase = true)
            }
        }

        state.selectedComunidad?.let { comunidad ->
            filtered = filtered.filter {
                it.comunidad.equals(comunidad, ignoreCase = true)
            }
        }

        state.selectedProvincia?.let { provincia ->
            filtered = filtered.filter {
                it.provincia.equals(provincia, ignoreCase = true)
            }
        }

        state.selectedMunicipio?.let { municipio ->
            filtered = filtered.filter {
                it.ciudad.equals(municipio, ignoreCase = true)
            }
        }

        val finalList = filtered.toList().sortedWith(
            when (state.distanciaSort) {
                DistanciaSort.CERCA_LEJOS -> compareBy<User> {
                    estimateDistanceKm(it, state.userLatitude, state.userLongitude)
                }

                DistanciaSort.LEJOS_CERCA -> compareByDescending<User> {
                    estimateDistanceKm(it, state.userLatitude, state.userLongitude)
                }
            }.then(
                when (state.nombreSort) {
                    NombreSort.A_Z -> compareBy { it.nombreProtectora.lowercase() }
                    NombreSort.Z_A -> compareByDescending { it.nombreProtectora.lowercase() }
                }
            )
        )

        _uiState.update {
            it.copy(filteredProtectoras = finalList)
        }
    }

    private fun estimateDistanceKm(
        protectora: User,
        userLatitude: Double?,
        userLongitude: Double?
    ): Double {
        if (userLatitude == null || userLongitude == null) return Double.MAX_VALUE
        if (protectora.latitude == 0.0 && protectora.longitude == 0.0) return Double.MAX_VALUE

        val earthRadius = 6371.0
        val dLat = Math.toRadians(protectora.latitude - userLatitude)
        val dLng = Math.toRadians(protectora.longitude - userLongitude)

        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(userLatitude)) *
                cos(Math.toRadians(protectora.latitude)) *
                sin(dLng / 2).pow(2.0)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}