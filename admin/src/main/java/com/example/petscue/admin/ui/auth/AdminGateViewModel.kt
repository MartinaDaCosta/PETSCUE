package com.example.petscue.admin.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminGateViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _sessionState =
        MutableStateFlow<AdminSessionState>(AdminSessionState.Loading)
    val sessionState: StateFlow<AdminSessionState> = _sessionState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _sessionState.value = AdminSessionState.Loading

            val currentUser = auth.currentUser
            if (currentUser == null) {
                _sessionState.value = AdminSessionState.LoggedOut
                return@launch
            }

            runCatching {
                val snapshot = db.collection("users")
                    .document(currentUser.uid)
                    .get()
                    .await()

                if (!snapshot.exists()) {
                    auth.signOut()
                    error("No existe profile para este usuario.")
                }

                val isAdmin = snapshot.getBoolean("admin") == true
                if (!isAdmin) {
                    auth.signOut()
                    error("Este usuario no tiene permisos de administrador.")
                }
            }.onSuccess {
                _sessionState.value = AdminSessionState.Authorized
            }.onFailure { e ->
                auth.signOut()
                _sessionState.value = AdminSessionState.Error(
                    e.message ?: "No se pudo validar la sesión."
                )
            }
        }
    }

    fun logout() {
        auth.signOut()
        _sessionState.value = AdminSessionState.LoggedOut
    }
}