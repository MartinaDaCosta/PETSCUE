package com.example.petscue.ui.novedades

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.User
import com.example.petscue.domain.usecase.GetPostsUseCase
import com.example.petscue.domain.usecase.InsertPostUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
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

                snapshot.toObject(User::class.java) ?: User(uid = uid)
            }.onSuccess { user ->
                _uiState.update { it.copy(currentUser = user) }
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getPostsUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { posts ->
                    _uiState.update { it.copy(posts = posts, isLoading = false) }
                }
        }
    }

    fun insertPost(post: Post) {
        viewModelScope.launch {
            insertPostUseCase(post)
        }
    }

    fun toggleLike(post: Post) {
        _uiState.update { state ->
            val liked = state.likedPostIds.contains(post.id)
            val updatedIds = if (liked) {
                state.likedPostIds - post.id
            } else {
                state.likedPostIds + post.id
            }

            val updatedPosts = state.posts.map {
                if (it.id == post.id) {
                    it.copy(
                        likes = if (liked) (it.likes - 1).coerceAtLeast(0) else it.likes + 1
                    )
                } else it
            }

            state.copy(
                likedPostIds = updatedIds,
                posts = updatedPosts
            )
        }
    }

    fun toggleSave(post: Post) {
        _uiState.update { state ->
            val saved = state.savedPostIds.contains(post.id)
            state.copy(
                savedPostIds = if (saved) state.savedPostIds - post.id else state.savedPostIds + post.id
            )
        }
    }

    fun toggleRepost(post: Post) {
        _uiState.update { state ->
            val reposted = state.repostedPostIds.contains(post.id)
            state.copy(
                repostedPostIds = if (reposted) state.repostedPostIds - post.id else state.repostedPostIds + post.id
            )
        }
    }

    fun addComment(post: Post, comment: String) {
        if (comment.isBlank()) return

        _uiState.update { state ->
            val updatedPosts = state.posts.map {
                if (it.id == post.id) {
                    it.copy(comentarios = it.comentarios + 1)
                } else it
            }
            state.copy(posts = updatedPosts)
        }
    }

    fun sharePost(post: Post) {
    }
}