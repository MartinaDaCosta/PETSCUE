package com.example.petscue.ui.mensajes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.repository.MensajesRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class MensajesViewModel @Inject constructor(
    private val mensajesRepository: MensajesRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val currentUserId = auth.currentUser?.uid.orEmpty()
    fun deleteConversation(conversationId: String) {
        if (conversationId.isBlank() || currentUserId.isBlank()) {
            return
        }

        viewModelScope.launch {
            runCatching {
                mensajesRepository.hideConversation(
                    conversationId = conversationId,
                    userId = currentUserId
                )
            }
        }
    }
    val uiState: StateFlow<MensajesUiState> =
        mensajesRepository.observeConversations(currentUserId)
            .map { conversations ->
                MensajesUiState(
                    isLoading = false,
                    conversations = conversations,
                    currentUserId = currentUserId
                )
            }
            .catch { error ->
                emit(
                    MensajesUiState(
                        isLoading = false,
                        currentUserId = currentUserId,
                        errorMessage = error.message
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MensajesUiState(
                    currentUserId = currentUserId
                )
            )
}
