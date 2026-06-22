package com.example.petscue.ui.profile.pet.petdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Pet
import com.example.petscue.data.repository.MensajesRepository
import com.example.petscue.data.repository.PetRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val mensajesRepository: MensajesRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: String = savedStateHandle.get<String>("petId").orEmpty()

    private val _uiState = MutableStateFlow(PetDetailUiState())
    val uiState: StateFlow<PetDetailUiState> = _uiState.asStateFlow()

    init {
        observePet()
    }

    private fun observePet() {
        if (petId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    pet = null,
                    isOwner = false,
                    error = "No se recibió el id de la mascota."
                )
            }
            return
        }

        petRepository
            .getAll()
            .onEach { pets ->
                val pet = pets.firstOrNull { currentPet -> currentPet.id == petId }
                val currentUserId = auth.currentUser?.uid.orEmpty()

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        pet = pet,
                        isOwner = pet != null &&
                                currentUserId.isNotBlank() &&
                                currentUserId == pet.userId,
                        error = if (pet == null) "No se encontró la mascota." else null
                    )
                }
            }
            .catch { e ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar la mascota."
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun openOrCreateGeneralChat() {
        val currentUserId = auth.currentUser?.uid ?: run {
            _uiState.update { it.copy(error = "Debes iniciar sesión para enviar mensajes.") }
            return
        }

        val pet = _uiState.value.pet ?: run {
            _uiState.update { it.copy(error = "No se encontró la mascota.") }
            return
        }

        if (_uiState.value.isOwner) {
            _uiState.update { it.copy(error = "No puedes enviarte un mensaje a ti mismo.") }
            return
        }

        val otherUserId = pet.userId
        if (otherUserId.isBlank()) {
            _uiState.update { it.copy(error = "La mascota no tiene un propietario válido.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                mensajesRepository.createOrGetGeneralConversation(
                    currentUserId = currentUserId,
                    otherUserId = otherUserId,
                    petId = pet.id,
                    petName = pet.nombre,
                    petImageUrl = pet.fotos.firstOrNull().orEmpty(),
                    petOwnerId = pet.userId
                )
            }.onSuccess { conversationId ->
                _uiState.update {
                    it.copy(openChatConversationId = conversationId)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(error = e.message ?: "No se pudo abrir el chat.")
                }
            }
        }
    }

    fun consumeOpenChatEvent() {
        _uiState.update {
            it.copy(openChatConversationId = null)
        }
    }

    fun deletePet() {
        val petToDelete = _uiState.value.pet

        if (petToDelete == null) {
            _uiState.update {
                it.copy(error = "No hay mascota para eliminar.")
            }
            return
        }

        if (!_uiState.value.isOwner) {
            _uiState.update {
                it.copy(error = "No tienes permisos para eliminar esta mascota.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    isLoading = true,
                    error = null
                )
            }

            runCatching {
                petRepository.delete(petToDelete)
            }.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        isDeleted = true
                    )
                }
            }.onFailure { e ->
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        error = e.message ?: "No se pudo eliminar la mascota."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { current ->
            current.copy(error = null)
        }
    }
}