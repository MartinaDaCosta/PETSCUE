package com.example.petscue.ui.mensajes.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Conversation
import com.example.petscue.data.repository.MensajesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val mensajesRepository: MensajesRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val conversationId: String =
        savedStateHandle.get<String>("conversationId").orEmpty()

    private val currentUserId: String =
        auth.currentUser?.uid.orEmpty()

    private val _uiState = MutableStateFlow(
        ChatUiState(
            isLoading = true,
            currentUserId = currentUserId
        )
    )
    val uiState = _uiState.asStateFlow()

    init {
        observeConversation()
        observeMessages()
        markAsRead()
    }

    private fun observeConversation() {
        firestore.collection("conversations")
            .document(conversationId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                    return@addSnapshotListener
                }

                val conversation = snapshot
                    ?.toObject(Conversation::class.java)
                    ?.copy(id = snapshot.id)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        conversation = conversation
                    )
                }
            }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            mensajesRepository.observeMessages(conversationId).collect { messages ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        messages = messages
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            val userSnapshot = firestore.collection("users")
                .document(currentUserId)
                .get()
                .await()

            val senderName = userSnapshot.getString("username")
                ?: userSnapshot.getString("name")
                ?: "Usuario"

            mensajesRepository.sendMessage(
                conversationId = conversationId,
                senderId = currentUserId,
                senderName = senderName,
                text = trimmed
            )
        }
    }

    private fun markAsRead() {
        if (conversationId.isBlank() || currentUserId.isBlank()) return

        viewModelScope.launch {
            mensajesRepository.markConversationAsRead(
                conversationId = conversationId,
                userId = currentUserId
            )
        }
    }
}