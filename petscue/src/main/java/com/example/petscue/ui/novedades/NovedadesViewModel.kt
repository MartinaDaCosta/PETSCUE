package com.example.petscue.ui.novedades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.Reply
import com.example.petscue.data.model.User
import com.example.petscue.data.repository.PostRepository
import com.example.petscue.data.repository.ReplyRepository
import com.example.petscue.domain.usecase.DeletePostUseCase
import com.example.petscue.domain.usecase.GetPostsUseCase
import com.example.petscue.domain.usecase.InsertPostUseCase
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
class NovedadesViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val insertPostUseCase: InsertPostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val postRepository: PostRepository,
    private val replyRepository: ReplyRepository,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(NovedadesUiState())
    val uiState: StateFlow<NovedadesUiState> = _uiState

    init {
        loadCurrentUser()
        loadPosts()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            runCatching {
                val uid = auth.currentUser?.uid ?: return@launch

                val snapshot = db.collection("users")
                    .document(uid)
                    .get()
                    .await()

                snapshot.toObject(User::class.java)
                    ?: User(uid = uid)
            }.onSuccess { user ->
                _uiState.update {
                    it.copy(currentUser = user)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        error = error.message
                            ?: "No se pudo cargar el usuario"
                    )
                }
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true)
            }

            getPostsUseCase()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                                ?: "No se pudieron cargar las publicaciones"
                        )
                    }
                }
                .collect { posts ->
                    _uiState.update {
                        it.copy(
                            posts = posts,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun insertPost(
        post: Post,
        localImageUris: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            runCatching {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        error = null
                    )
                }

                insertPostUseCase(
                    post = post,
                    localImageUris = localImageUris
                )
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message
                            ?: "No se pudo publicar"
                    )
                }
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            runCatching {
                deletePostUseCase(post)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        error = error.message
                            ?: "No se pudo borrar la publicación"
                    )
                }
            }
        }
    }

    fun toggleLike(post: Post) {
        val userId = _uiState.value.currentUser.uid

        toggleInteraction(
            post = post,
            userId = userId,
            type = InteractionType.LIKE
        )
    }

    fun toggleRepost(post: Post) {
        val userId = _uiState.value.currentUser.uid

        toggleInteraction(
            post = post,
            userId = userId,
            type = InteractionType.REPOST
        )
    }

    fun sharePost(post: Post) {
        val userId = _uiState.value.currentUser.uid

        toggleInteraction(
            post = post,
            userId = userId,
            type = InteractionType.SHARE
        )
    }

    private fun toggleInteraction(
        post: Post,
        userId: String,
        type: InteractionType
    ) {
        if (userId.isBlank() || post.id.isBlank()) {
            _uiState.update {
                it.copy(
                    error = "Debes iniciar sesión para realizar esta acción"
                )
            }
            return
        }

        val updatedPost = when (type) {
            InteractionType.LIKE -> {
                val updatedLikedBy = if (post.likedBy.contains(userId)) {
                    post.likedBy - userId
                } else {
                    post.likedBy + userId
                }

                post.copy(
                    likedBy = updatedLikedBy,
                    likes = updatedLikedBy.size
                )
            }

            InteractionType.REPOST -> {
                val updatedRepostedBy = if (post.repostedBy.contains(userId)) {
                    post.repostedBy - userId
                } else {
                    post.repostedBy + userId
                }

                post.copy(repostedBy = updatedRepostedBy)
            }

            InteractionType.SHARE -> {
                val updatedSharedBy = if (post.sharedBy.contains(userId)) {
                    post.sharedBy - userId
                } else {
                    post.sharedBy + userId
                }

                post.copy(sharedBy = updatedSharedBy)
            }
        }

        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { currentPost ->
                    if (currentPost.id == post.id) {
                        updatedPost
                    } else {
                        currentPost
                    }
                },
                error = null
            )
        }

        viewModelScope.launch {
            runCatching {
                when (type) {
                    InteractionType.LIKE -> {
                        postRepository.toggleLike(post.id, userId)
                    }

                    InteractionType.REPOST -> {
                        postRepository.toggleRepost(post.id, userId)
                    }

                    InteractionType.SHARE -> {
                        postRepository.toggleShare(post.id, userId)
                    }
                }
            }.onFailure { error ->
                _uiState.update { state ->
                    state.copy(
                        posts = state.posts.map { currentPost ->
                            if (currentPost.id == post.id) {
                                post
                            } else {
                                currentPost
                            }
                        },
                        error = error.message
                            ?: "No se pudo actualizar la publicación"
                    )
                }
            }
        }
    }

    fun addComment(
        post: Post,
        comment: String
    ) {
        val text = comment.trim()
        val user = _uiState.value.currentUser

        if (text.isBlank() || post.id.isBlank()) return

        if (user.uid.isBlank()) {
            _uiState.update {
                it.copy(
                    error = "Debes iniciar sesión para comentar"
                )
            }
            return
        }

        viewModelScope.launch {
            val userName = listOf(user.nombre, user.apellido)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { "Usuario Petscue" }

            val userHandle = user.username
                .trim()
                .let { username ->
                    if (username.isBlank()) "@usuario" else "@$username"
                }

            val reply = Reply(
                id = UUID.randomUUID().toString(),
                postId = post.id,
                parentReplyId = null,
                userId = user.uid,
                userName = userName,
                userHandle = userHandle,
                userAvatar = user.photoUrl,
                mensaje = text,
                timestamp = System.currentTimeMillis(),
                likes = 0,
                likedBy = emptyList(),
                sharedBy = emptyList()
            )

            runCatching {
                replyRepository.insertReply(
                    postId = post.id,
                    reply = reply
                )

                db.collection("posts")
                    .document(post.id)
                    .update(
                        "comentarios",
                        FieldValue.increment(1)
                    )
                    .await()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        error = error.message
                            ?: "No se pudo enviar el comentario"
                    )
                }
            }
        }
    }

    fun toggleSave(post: Post) {
        _uiState.update { state ->
            val isSaved = state.savedPostIds.contains(post.id)

            state.copy(
                savedPostIds = if (isSaved) {
                    state.savedPostIds - post.id
                } else {
                    state.savedPostIds + post.id
                }
            )
        }
    }
}

private enum class InteractionType {
    LIKE,
    REPOST,
    SHARE
}