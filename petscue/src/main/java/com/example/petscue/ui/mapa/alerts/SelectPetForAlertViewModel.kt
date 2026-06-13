package com.example.petscue.ui.mapa.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.repository.PetRepository
import com.example.petscue.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectPetForAlertViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectPetForAlertUiState())
    val uiState: StateFlow<SelectPetForAlertUiState> = _uiState.asStateFlow()

    init {
        observePets()
    }

    private fun observePets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                profileRepository.getCurrentUserProfile()
            }.onSuccess { user ->
                petRepository.getByUserId(user.uid)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "No se pudieron cargar tus mascotas."
                            )
                        }
                    }
                    .collectLatest { pets ->
                        _uiState.update {
                            it.copy(
                                pets = pets,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "No se pudo cargar el usuario."
                    )
                }
            }
        }
    }
}