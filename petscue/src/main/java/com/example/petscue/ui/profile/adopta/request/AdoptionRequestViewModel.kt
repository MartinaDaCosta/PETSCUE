package com.example.petscue.ui.profile.adopta.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Pet
import com.example.petscue.data.repository.MensajesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@HiltViewModel
class AdoptionRequestViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val firestore: FirebaseFirestore,
    private val mensajesRepository: MensajesRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val petId: String = savedStateHandle["petId"] ?: ""

    private val _uiState = MutableStateFlow(AdoptionRequestUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPet()
    }

    private fun loadPet() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                val snapshot = firestore.collection("adoption_pets")
                    .document(petId)
                    .get()
                    .await()

                snapshot.toObject(Pet::class.java)?.copy(id = snapshot.id)
            }.onSuccess { pet ->
                _uiState.update {
                    it.copy(
                        pet = pet,
                        isLoading = false
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "No se pudo cargar el animal"
                    )
                }
            }
        }
    }

    fun onMensajeChange(value: String) {
        _uiState.update { it.copy(mensaje = value) }
    }

    fun onTelefonoChange(value: String) {
        _uiState.update { it.copy(telefono = value) }
    }

    fun onViviendaChange(value: String) {
        _uiState.update { it.copy(vivienda = value) }
    }

    fun onExperienciaChange(value: String) {
        _uiState.update { it.copy(experiencia = value) }
    }

    fun onOtrosAnimalesChange(value: String) {
        _uiState.update { it.copy(otrosAnimales = value) }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(submittedConversationId = null) }
    }

    fun submitRequest() {
        val currentUserId = auth.currentUser?.uid ?: run {
            _uiState.update { it.copy(error = "Debes iniciar sesión") }
            return
        }

        val pet = _uiState.value.pet ?: run {
            _uiState.update { it.copy(error = "No se ha encontrado el animal") }
            return
        }

        if (_uiState.value.telefono.isBlank() || _uiState.value.vivienda.isBlank()) {
            _uiState.update {
                it.copy(error = "Completa al menos teléfono y vivienda")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            runCatching {
                val userSnapshot = firestore.collection("users")
                    .document(currentUserId)
                    .get()
                    .await()

                val userName = userSnapshot.getString("username")
                    ?.takeIf { it.isNotBlank() }
                    ?: userSnapshot.getString("nombre")
                        ?.takeIf { it.isNotBlank() }
                    ?: "Usuario"

                val shelterId = pet.userId

                val requestId = firestore.collection("adoption_requests")
                    .document()
                    .id

                val now = System.currentTimeMillis()

                val request = hashMapOf(
                    "id" to requestId,
                    "animalId" to pet.id,
                    "animalNombre" to pet.nombre,
                    "animalImagen" to pet.fotos.firstOrNull().orEmpty(),
                    "protectoraId" to shelterId,
                    "userId" to currentUserId,
                    "userNombre" to userName,
                    "mensaje" to _uiState.value.mensaje,
                    "telefono" to _uiState.value.telefono,
                    "vivienda" to _uiState.value.vivienda,
                    "experiencia" to _uiState.value.experiencia,
                    "otrosAnimales" to _uiState.value.otrosAnimales,
                    "estado" to "PENDIENTE",
                    "createdAt" to now
                )

                firestore.collection("adoption_requests")
                    .document(requestId)
                    .set(request)
                    .await()

                val conversationId = mensajesRepository.createOrGetAdoptionConversation(
                    currentUserId = currentUserId,
                    shelterId = shelterId,
                    petId = pet.id,
                    petName = pet.nombre,
                    petImageUrl = pet.fotos.firstOrNull().orEmpty(),
                    adoptionFormId = requestId,
                    adoptionFormStatus = "PENDIENTE"
                )

                val summary = """
                    Solicitud de adopción enviada
                    
                    Mensaje: ${_uiState.value.mensaje.ifBlank { "-" }}
                    Teléfono: ${_uiState.value.telefono.ifBlank { "-" }}
                    Vivienda: ${_uiState.value.vivienda.ifBlank { "-" }}
                    Experiencia: ${_uiState.value.experiencia.ifBlank { "-" }}
                    Otros animales: ${_uiState.value.otrosAnimales.ifBlank { "-" }}
                """.trimIndent()

                mensajesRepository.sendMessage(
                    conversationId = conversationId,
                    senderId = currentUserId,
                    senderName = userName,
                    text = summary
                )

                conversationId
            }.onSuccess { conversationId ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submittedConversationId = conversationId
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = e.message ?: "No se pudo enviar la solicitud"
                    )
                }
            }
        }
    }
}