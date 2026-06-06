package com.example.petscue.admin.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminLoginViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AdminLoginUiState())
    val uiState: StateFlow<AdminLoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val email = uiState.value.email.trim()
        val password = uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Introduce email y contraseña.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            runCatching {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: error("No se pudo obtener el usuario.")

                val snapshot = db.collection("users")
                    .document(uid)
                    .get()
                    .await()

                if (!snapshot.exists()) {
                    auth.signOut()
                    error("Tu usuario no tiene profile en users/$uid.")
                }

                val isAdmin = snapshot.getBoolean("admin") == true
                if (!isAdmin) {
                    auth.signOut()
                    error("No tienes permisos de administrador.")
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(isLoading = false, loginSuccess = true)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error al iniciar sesión."
                    )
                }
            }
        }
    }
}