package com.example.petscue.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.UserRole
import com.example.petscue.data.repository.MensajesRepository
import com.example.petscue.data.repository.PostRepository
import com.example.petscue.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val mensajesRepository: MensajesRepository,
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val viewedUserId: String? = savedStateHandle["userId"]

    private val _uiState = MutableStateFlow(
        ProfileUiState(isLoading = true)
    )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var petsJob: Job? = null
    private var likedPostsJob: Job? = null
    private var postsJob: Job? = null
    private var repliesJob: Job? = null
    private var repostedPostsJob: Job? = null
    private var ownPosts: List<Post> = emptyList()
    private var repostedPosts: List<Post> = emptyList()
    private val _openChatEvent = MutableStateFlow<String?>(null)
    val openChatEvent: StateFlow<String?> = _openChatEvent.asStateFlow()

    init {
        refreshProfile()
    }

    fun openOrCreateGeneralChat() {
        viewModelScope.launch {
            val currentUser = runCatching {
                repository.getCurrentUserProfile()
            }.getOrElse { error ->
                _uiState.update {
                    it.copy(
                        error = error.message
                            ?: "No se pudo obtener el usuario actual"
                    )
                }
                return@launch
            }

            val otherUserId = viewedUserId ?: return@launch

            if (currentUser.uid == otherUserId) {
                return@launch
            }

            runCatching {
                mensajesRepository.createOrGetGeneralConversation(
                    currentUserId = currentUser.uid,
                    otherUserId = otherUserId
                )
            }.onSuccess { conversationId ->
                _openChatEvent.value = conversationId
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        error = error.message
                            ?: "No se pudo abrir la conversación"
                    )
                }
            }
        }
    }

    fun consumeOpenChatEvent() {
        _openChatEvent.value = null
    }

    fun onTabSelected(tab: ProfileTab) {
        _uiState.update {
            it.copy(selectedTab = tab)
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, error = null)
            }

            runCatching {
                val currentUser = repository.getCurrentUserProfile()

                val user = if (
                    viewedUserId.isNullOrBlank()
                    || viewedUserId == currentUser.uid
                ) {
                    currentUser
                } else {
                    repository.getUserProfileById(viewedUserId)
                }

                val followersCount = runCatching {
                    repository.getFollowersCount(user.uid)
                }.getOrDefault(0)

                val followingCount = runCatching {
                    repository.getFollowingCount(user.uid)
                }.getOrDefault(0)

                val isFollowing = if (currentUser.uid != user.uid) {
                    runCatching {
                        repository.isFollowing(currentUser.uid, user.uid)
                    }.getOrDefault(false)
                } else {
                    false
                }



                ProfileInitialData(
                    currentUserId = currentUser.uid,
                    viewedUser = user,
                    followersCount = followersCount,
                    followingCount = followingCount,
                    isFollowing = isFollowing
                )
            }.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUserId = data.currentUserId,
                        user = data.viewedUser,
                        followersCount = data.followersCount,
                        followingCount = data.followingCount,
                        isFollowing = data.isFollowing
                    )
                }

                observePosts(data.viewedUser.uid)
                observeRepostedPosts(data.viewedUser.uid)
                observeLikedPosts(data.viewedUser.uid)
                observePets(data.viewedUser.uid, data.viewedUser.role)
                observeReplies(data.viewedUser.uid)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "No se pudo cargar el perfil"
                    )
                }
            }
        }
    }
    private fun observeReplies(userId: String) {
        repliesJob?.cancel()

        repliesJob = viewModelScope.launch {
            repository.getRepliesByUser(userId)
                .collectLatest { replies ->
                    _uiState.update {
                        it.copy(
                            replies = replies
                        )
                    }
                }
        }
    }
    private fun observePosts(userId: String) {
        postsJob?.cancel()

        postsJob = viewModelScope.launch {
            repository.getPostsByUser(userId)
                .collectLatest { posts ->
                    ownPosts = posts
                    updateProfilePosts()
                }
        }
    }
    private fun observeRepostedPosts(userId: String) {
        repostedPostsJob?.cancel()

        repostedPostsJob = viewModelScope.launch {
            repository.getRepostedPostsByUser(userId)
                .collectLatest { posts ->
                    repostedPosts = posts
                    updateProfilePosts()
                }
        }
    }
    private fun updateProfilePosts() {
        val allPosts = (ownPosts + repostedPosts)
            .distinctBy { post -> post.id }
            .sortedByDescending { post -> post.timestamp }

        _uiState.update {
            it.copy(
                posts = allPosts,
                mediaPosts = allPosts.filter { post ->
                    post.fotos.isNotEmpty()
                }
            )
        }
    }
    private fun observeLikedPosts(userId: String) {
        likedPostsJob?.cancel()

        likedPostsJob = viewModelScope.launch {
            repository.getLikedPostsByUser(userId)
                .collectLatest { likedPosts ->
                    _uiState.update {
                        it.copy(
                            likedPosts = likedPosts.sortedByDescending { post ->
                                post.timestamp
                            }
                        )
                    }
                }
        }
    }

    private fun observePets(
        userId: String,
        role: UserRole
    ) {
        petsJob?.cancel()

        petsJob = viewModelScope.launch {
            if (role == UserRole.PROTECTORA) {
                repository.getAdoptionPetsByProtectora(userId)
                    .collectLatest { adoptionPets ->
                        _uiState.update {
                            it.copy(
                                pets = emptyList(),
                                adoptionPets = adoptionPets
                            )
                        }
                    }
            } else {
                repository.getPetsByUser(userId)
                    .collectLatest { pets ->
                        _uiState.update {
                            it.copy(
                                pets = pets,
                                adoptionPets = emptyList()
                            )
                        }
                    }
            }
        }
    }

    fun toggleLike(post: Post) {
        togglePostInteraction(
            post = post,
            field = PostInteraction.LIKE
        )
    }

    fun toggleRepost(post: Post) {
        togglePostInteraction(
            post = post,
            field = PostInteraction.REPOST
        )
    }

    fun toggleShare(post: Post) {
        togglePostInteraction(
            post = post,
            field = PostInteraction.SHARE
        )
    }

    private fun togglePostInteraction(
        post: Post,
        field: PostInteraction
    ) {
        val currentUserId = _uiState.value.currentUserId

        if (currentUserId.isBlank() || post.id.isBlank()) {
            _uiState.update {
                it.copy(
                    error = "Debes iniciar sesión para realizar esta acción"
                )
            }
            return
        }

        val updatedPost = when (field) {
            PostInteraction.LIKE -> {
                val isLiked = post.likedBy.contains(currentUserId)
                val updatedLikedBy = if (isLiked) {
                    post.likedBy - currentUserId
                } else {
                    post.likedBy + currentUserId
                }

                post.copy(
                    likedBy = updatedLikedBy,
                    likes = updatedLikedBy.size
                )
            }

            PostInteraction.REPOST -> {
                val isReposted = post.repostedBy.contains(currentUserId)

                post.copy(
                    repostedBy = if (isReposted) {
                        post.repostedBy - currentUserId
                    } else {
                        post.repostedBy + currentUserId
                    }
                )
            }

            PostInteraction.SHARE -> {
                val isShared = post.sharedBy.contains(currentUserId)

                post.copy(
                    sharedBy = if (isShared) {
                        post.sharedBy - currentUserId
                    } else {
                        post.sharedBy + currentUserId
                    }
                )
            }
        }

        applyOptimisticPostUpdate(
            originalPost = post,
            updatedPost = updatedPost,
            interaction = field
        )

        viewModelScope.launch {
            runCatching {
                when (field) {
                    PostInteraction.LIKE -> {
                        postRepository.toggleLike(post.id, currentUserId)
                    }

                    PostInteraction.REPOST -> {
                        postRepository.toggleRepost(post.id, currentUserId)
                    }

                    PostInteraction.SHARE -> {
                        postRepository.toggleShare(post.id, currentUserId)
                    }
                }
            }.onFailure { error ->
                restorePostAfterError(
                    originalPost = post,
                    interaction = field,
                    errorMessage = error.message
                        ?: "No se pudo actualizar la publicación"
                )
            }
        }
    }

    private fun applyOptimisticPostUpdate(
        originalPost: Post,
        updatedPost: Post,
        interaction: PostInteraction
    ) {
        _uiState.update { current ->
            val newLikedPosts = when (interaction) {
                PostInteraction.LIKE -> {
                    if (updatedPost.likedBy.contains(current.currentUserId)) {
                        current.likedPosts.replacePost(updatedPost)
                            .ifEmpty { listOf(updatedPost) }
                    } else {
                        current.likedPosts.filterNot { it.id == originalPost.id }
                    }
                }

                else -> current.likedPosts.replacePost(updatedPost)
            }

            current.copy(
                posts = current.posts.replacePost(updatedPost),
                mediaPosts = current.mediaPosts.replacePost(updatedPost),
                likedPosts = newLikedPosts,
                error = null
            )
        }
    }

    private fun restorePostAfterError(
        originalPost: Post,
        interaction: PostInteraction,
        errorMessage: String
    ) {
        _uiState.update { current ->
            val restoredLikedPosts = when (interaction) {
                PostInteraction.LIKE -> {
                    if (originalPost.likedBy.contains(current.currentUserId)) {
                        current.likedPosts.replacePost(originalPost)
                            .ifEmpty { listOf(originalPost) }
                    } else {
                        current.likedPosts.filterNot { it.id == originalPost.id }
                    }
                }

                else -> current.likedPosts.replacePost(originalPost)
            }

            current.copy(
                posts = current.posts.replacePost(originalPost),
                mediaPosts = current.mediaPosts.replacePost(originalPost),
                likedPosts = restoredLikedPosts,
                error = errorMessage
            )
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            val state = _uiState.value
            val currentUserId = state.currentUserId
            val profileUserId = state.user?.uid ?: return@launch

            if (
                currentUserId.isBlank()
                || profileUserId.isBlank()
                || currentUserId == profileUserId
            ) {
                return@launch
            }

            runCatching {
                if (state.isFollowing) {
                    repository.unfollowUser(
                        followerId = currentUserId,
                        followedId = profileUserId
                    )
                } else {
                    repository.followUser(
                        followerId = currentUserId,
                        followedId = profileUserId
                    )
                }
            }.onSuccess {
                _uiState.update { current ->
                    current.copy(
                        isFollowing = !state.isFollowing,
                        followersCount = if (state.isFollowing) {
                            (current.followersCount - 1).coerceAtLeast(0)
                        } else {
                            current.followersCount + 1
                        }
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        error = error.message
                            ?: "No se pudo actualizar el seguimiento"
                    )
                }
            }
        }
    }

    fun startConversation(onReady: (String) -> Unit) {
        val targetUserId = viewedUserId ?: return

        viewModelScope.launch {
            runCatching {
                val currentUser = repository.getCurrentUserProfile()

                mensajesRepository.createOrGetGeneralConversation(
                    currentUserId = currentUser.uid,
                    otherUserId = targetUserId
                )
            }.onSuccess(onReady)
        }
    }

    override fun onCleared() {
        petsJob?.cancel()
        likedPostsJob?.cancel()
        postsJob?.cancel()
        repliesJob?.cancel()
        repostedPostsJob?.cancel()
        super.onCleared()
    }
}

private data class ProfileInitialData(
    val currentUserId: String,
    val viewedUser: com.example.petscue.data.model.User,
    val followersCount: Int,
    val followingCount: Int,
    val isFollowing: Boolean
)

private enum class PostInteraction {
    LIKE,
    REPOST,
    SHARE
}

private fun List<Post>.replacePost(updatedPost: Post): List<Post> {
    return map { currentPost ->
        if (currentPost.id == updatedPost.id) {
            updatedPost
        } else {
            currentPost
        }
    }
}