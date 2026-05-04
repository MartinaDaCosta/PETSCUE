package com.example.petscue.ui.mascotas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Pet
import com.example.petscue.domain.usecase.GetPetsUseCase
import com.example.petscue.domain.usecase.InsertPetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MascotasViewModel @Inject constructor(
    private val getPetsUseCase: GetPetsUseCase,
    private val insertPetUseCase: InsertPetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MascotasUiState())
    val uiState: StateFlow<MascotasUiState> = _uiState.asStateFlow()

    init {
        cargarPets("perdido")
    }

    fun setFiltro(filtro: String) {
        _uiState.update { it.copy(filtroActivo = filtro) }
        cargarPets(filtro)
    }

    private fun cargarPets(estado: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPetsUseCase(estado)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { pets ->
                    _uiState.update { it.copy(pets = pets, isLoading = false) }
                }
        }
    }

    fun insertPet(pet: Pet) {
        viewModelScope.launch {
            insertPetUseCase(pet)
        }
    }
}