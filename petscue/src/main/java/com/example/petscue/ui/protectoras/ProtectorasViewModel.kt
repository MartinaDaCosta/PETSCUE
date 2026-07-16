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
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class ProtectorasViewModel @Inject constructor(
    private val repository: ProtectorasRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProtectorasUiState(isLoading = true)
    )

    val uiState: StateFlow<ProtectorasUiState> = _uiState.asStateFlow()

    init {
        loadProtectoras()
    }

    private fun loadProtectoras() {
        viewModelScope.launch {
            runCatching {
                repository.getProtectoras()
            }.onSuccess { protectoras ->
                _uiState.update {
                    it.copy(
                        allProtectoras = protectoras,
                        comunidadesDisponibles = protectoras
                            .map { user -> user.comunidad.trim() }
                            .filter { comunidad -> comunidad.isNotBlank() }
                            .distinctBy { comunidad ->
                                normalize(comunidad)
                            }
                            .sortedBy { comunidad ->
                                normalize(comunidad)
                            },
                        isLoading = false,
                        error = null
                    )
                }

                refreshLocationFilters()
                updateSuggestions()
                applyFilters()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message
                            ?: "No se han podido cargar las protectoras"
                    )
                }
            }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.update {
            it.copy(query = query)
        }

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
        _uiState.update {
            it.copy(nombreSort = sort)
        }

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
        _uiState.update {
            it.copy(selectedMunicipio = municipio)
        }

        applyFilters()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                query = "",
                suggestions = emptyList(),
                nombreSort = NombreSort.A_Z,
                selectedComunidad = null,
                selectedProvincia = null,
                selectedMunicipio = null
            )
        }

        refreshLocationFilters()
        applyFilters()
    }

    private fun updateSuggestions() {
        val state = _uiState.value

        if (state.query.isBlank()) {
            _uiState.update {
                it.copy(suggestions = emptyList())
            }
            return
        }

        val normalizedQuery = normalize(state.query)

        val suggestions = state.allProtectoras
            .flatMap { protectora ->
                listOf(
                    protectora.nombreProtectora.trim(),
                    protectora.username.trim()
                )
            }
            .filter { value ->
                value.isNotBlank() &&
                        normalize(value).contains(normalizedQuery)
            }
            .distinctBy { value ->
                normalize(value)
            }
            .sortedBy { value ->
                normalize(value)
            }
            .take(5)

        _uiState.update {
            it.copy(suggestions = suggestions)
        }
    }

    private fun refreshLocationFilters() {
        val state = _uiState.value

        val provincias = state.allProtectoras
            .asSequence()
            .filter { protectora ->
                state.selectedComunidad.isNullOrBlank() ||
                        sameText(
                            protectora.comunidad,
                            state.selectedComunidad
                        )
            }
            .map { protectora ->
                protectora.provincia.trim()
            }
            .filter { provincia ->
                provincia.isNotBlank()
            }
            .distinctBy { provincia ->
                normalize(provincia)
            }
            .sortedBy { provincia ->
                normalize(provincia)
            }
            .toList()

        val municipios = state.allProtectoras
            .asSequence()
            .filter { protectora ->
                (state.selectedComunidad.isNullOrBlank() ||
                        sameText(
                            protectora.comunidad,
                            state.selectedComunidad
                        )) &&
                        (state.selectedProvincia.isNullOrBlank() ||
                                sameText(
                                    protectora.provincia,
                                    state.selectedProvincia
                                ))
            }
            .map { protectora ->
                protectora.ciudad.trim()
            }
            .filter { municipio ->
                municipio.isNotBlank()
            }
            .distinctBy { municipio ->
                normalize(municipio)
            }
            .sortedBy { municipio ->
                normalize(municipio)
            }
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
        val normalizedQuery = normalize(state.query)

        val filtered = state.allProtectoras
            .asSequence()
            .filter { protectora ->
                normalizedQuery.isBlank() ||
                        normalize(protectora.nombreProtectora)
                            .contains(normalizedQuery) ||
                        normalize(protectora.username)
                            .contains(normalizedQuery)
            }
            .filter { protectora ->
                state.selectedComunidad.isNullOrBlank() ||
                        sameText(
                            protectora.comunidad,
                            state.selectedComunidad
                        )
            }
            .filter { protectora ->
                state.selectedProvincia.isNullOrBlank() ||
                        sameText(
                            protectora.provincia,
                            state.selectedProvincia
                        )
            }
            .filter { protectora ->
                state.selectedMunicipio.isNullOrBlank() ||
                        sameText(
                            protectora.ciudad,
                            state.selectedMunicipio
                        )
            }
            .toList()

        val sorted = when (state.nombreSort) {
            NombreSort.A_Z -> filtered.sortedBy { protectora ->
                normalize(protectora.nombreProtectora)
            }

            NombreSort.Z_A -> filtered.sortedByDescending { protectora ->
                normalize(protectora.nombreProtectora)
            }
        }

        _uiState.update {
            it.copy(filteredProtectoras = sorted)
        }
    }

    private fun sameText(
        first: String,
        second: String?
    ): Boolean {
        return !second.isNullOrBlank() &&
                normalize(first) == normalize(second)
    }

    private fun normalize(value: String): String {
        return Normalizer
            .normalize(
                value.trim().lowercase(),
                Normalizer.Form.NFD
            )
            .replace("\\p{M}+".toRegex(), "")
    }
}