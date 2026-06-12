package com.example.petscue.ui.novedades.detailpost

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Reply
import com.example.petscue.data.model.User
import com.example.petscue.data.repository.PostRepository
import com.example.petscue.data.repository.ReplyRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
    private val replyRepository: ReplyRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow(PostDetailUiState(isLoading = true))
    val uiState: StateFlow<PostDetailUiState> = _uiState

    private var currentUser: User = User()

    init {
        _uiState.update { it.copy(currentUserId = auth.currentUser?.uid.orEmpty()) }
        loadCurrentUser()
        observePost()
        observeReplies()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            runCatching {
                val uid = auth.currentUser?.uid ?: return@launch
                val snapshot = db.collection("users")
                    .document(uid)
                    .get()
                    .await()

                snapshot.toObject(User::class.java) ?: User(uid = uid)
            }.onSuccess { user ->
                currentUser = user
            }
        }
    }

    private fun observePost() {
        viewModelScope.launch {
            postRepository.getAll()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { posts ->
                    _uiState.update {
                        it.copy(
                            post = posts.firstOrNull { post -> post.id == postId },
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun observeReplies() {
        viewModelScope.launch {
            replyRepository.getReplies(postId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { replies ->
                    _uiState.update { it.copy(replies = replies) }
                }
        }
    }

    fun updateReplyText(text: String) {
        _uiState.update { it.copy(replyText = text) }
    }

    fun setReplyingTo(reply: Reply?) {
        _uiState.update { it.copy(replyingTo = reply) }
    }
    fun deleteReply(reply: Reply) {
        viewModelScope.launch {
            runCatching {
                val postId = uiState.value.post?.id ?: return@launch

                // 1. Borrar hijos del reply
                val children = db.collection("posts")
                    .document(postId)
                    .collection("replies")
                    .whereEqualTo("parentReplyId", reply.id)
                    .get()
                    .await()

                children.documents.forEach { it.reference.delete().await() }

                // 2. Borrar el reply raíz
                db.collection("posts")
                    .document(postId)
                    .collection("replies")
                    .document(reply.id)
                    .delete()
                    .await()

                // 3. Decrementar contador de comentarios en el post
                val delta = -(1 + children.size())
                db.collection("posts")
                    .document(postId)
                    .update("comentarios", FieldValue.increment(delta.toLong()))
                    .await()

            }.onFailure { e ->
                _uiState.update { it.copy(error = "No se pudo borrar: ${e.message}") }
            }
        }
    }
    fun sendReply() {
        val state = _uiState.value
        val post = state.post ?: return
        if (state.replyText.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }

            val userName = listOf(currentUser.nombre, currentUser.apellido)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { "Usuario Petscue" }

            val userHandle = currentUser.username
                .trim()
                .let { if (it.isBlank()) "@usuario" else "@$it" }

            val reply = Reply(
                id = UUID.randomUUID().toString(),
                postId = post.id,
                parentReplyId = state.replyingTo?.id,
                userId = currentUser.uid.ifBlank { "demo_user" },
                userName = userName,
                userHandle = userHandle,
                userAvatar = currentUser.photoUrl,
                mensaje = state.replyText.trim(),
                timestamp = System.currentTimeMillis(),
                likes = 0
            )

            runCatching {
                replyRepository.insertReply(post.id, reply)
                db.collection("posts")
                    .document(post.id)
                    .update("comentarios", post.comentarios + 1)
                    .await()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        replyText = "",
                        replyingTo = null,
                        isSending = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        error = error.message
                    )
                }
            }
        }
    }
}