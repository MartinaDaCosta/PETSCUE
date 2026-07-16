package com.example.petscue.ui.profile.edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.UserRole
import com.example.petscue.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())

    val uiState: StateFlow<EditProfileUiState> =
        _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                repository.getCurrentUserProfile()
            }.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        role = user.role,

                        nombre = user.nombre,
                        apellido = user.apellido,

                        nombreProtectora = user.nombreProtectora,
                        descripcionProtectora = user.descripcionProtectora,
                        telefono = user.telefono,
                        direccion = user.direccion,
                        web = user.web,
                        instagram = user.instagram,
                        facebook = user.facebook,

                        currentPhotoUrl = user.photoUrl,
                        selectedPhotoUri = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message
                            ?: "No se pudo cargar el perfil."
                    )
                }
            }
        }
    }

    fun onNombreChange(value: String) {
        _uiState.update {
            it.copy(
                nombre = value,
                errorMessage = null
            )
        }
    }

    fun onApellidoChange(value: String) {
        _uiState.update {
            it.copy(
                apellido = value,
                errorMessage = null
            )
        }
    }

    fun onNombreProtectoraChange(value: String) {
        _uiState.update {
            it.copy(
                nombreProtectora = value,
                errorMessage = null
            )
        }
    }

    fun onDescripcionChange(value: String) {
        _uiState.update {
            it.copy(
                descripcionProtectora = value,
                errorMessage = null
            )
        }
    }

    fun onTelefonoChange(value: String) {
        _uiState.update {
            it.copy(
                telefono = value,
                errorMessage = null
            )
        }
    }

    fun onDireccionChange(value: String) {
        _uiState.update {
            it.copy(
                direccion = value,
                errorMessage = null
            )
        }
    }

    fun onWebChange(value: String) {
        _uiState.update {
            it.copy(
                web = value,
                errorMessage = null
            )
        }
    }

    fun onInstagramChange(value: String) {
        _uiState.update {
            it.copy(
                instagram = value,
                errorMessage = null
            )
        }
    }

    fun onFacebookChange(value: String) {
        _uiState.update {
            it.copy(
                facebook = value,
                errorMessage = null
            )
        }
    }

    fun onPhotoSelected(uri: Uri) {
        _uiState.update {
            it.copy(
                selectedPhotoUri = uri,
                errorMessage = null
            )
        }
    }

    fun saveProfile() {
        viewModelScope.launch {
            val state = _uiState.value
            val uid = auth.currentUser?.uid

            if (uid == null) {
                _uiState.update {
                    it.copy(
                        errorMessage = "No hay una sesión activa."
                    )
                }
                return@launch
            }

            if (
                state.role == UserRole.PROTECTORA &&
                state.nombreProtectora.trim().isBlank()
            ) {
                _uiState.update {
                    it.copy(
                        errorMessage = "El nombre de la protectora es obligatorio."
                    )
                }
                return@launch
            }

            if (
                state.role != UserRole.PROTECTORA &&
                state.nombre.trim().isBlank()
            ) {
                _uiState.update {
                    it.copy(
                        errorMessage = "El nombre es obligatorio."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null
                )
            }

            runCatching {
                val photoUrl = uploadPhotoIfNeeded(
                    uid = uid,
                    selectedPhotoUri = state.selectedPhotoUri,
                    currentPhotoUrl = state.currentPhotoUrl
                )

                val updates: Map<String, Any> =
                    if (state.role == UserRole.PROTECTORA) {
                        mapOf(
                            "nombreProtectora" to state.nombreProtectora.trim(),
                            "descripcionProtectora" to state.descripcionProtectora.trim(),
                            "telefono" to state.telefono.trim(),
                            "direccion" to state.direccion.trim(),
                            "web" to state.web.trim(),
                            "instagram" to state.instagram.trim(),
                            "facebook" to state.facebook.trim(),
                            "photoUrl" to photoUrl
                        )
                    } else {
                        mapOf(
                            "nombre" to state.nombre.trim(),
                            "apellido" to state.apellido.trim(),
                            "telefono" to state.telefono.trim(),
                            "direccion" to state.direccion.trim(),
                            "photoUrl" to photoUrl
                        )
                    }

                db.collection("users")
                    .document(uid)
                    .update(updates)
                    .await()

                photoUrl
            }.onSuccess { photoUrl ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        success = true,
                        currentPhotoUrl = photoUrl,
                        selectedPhotoUri = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message
                            ?: "No se pudo guardar el perfil."
                    )
                }
            }
        }
    }

    private suspend fun uploadPhotoIfNeeded(
        uid: String,
        selectedPhotoUri: Uri?,
        currentPhotoUrl: String
    ): String {
        if (selectedPhotoUri == null) {
            return currentPhotoUrl
        }

        val fileName = "${UUID.randomUUID()}.jpg"

        val reference = storage.reference
            .child("profile_images")
            .child(uid)
            .child(fileName)

        reference.putFile(selectedPhotoUri).await()

        return reference.downloadUrl.await().toString()
    }
}