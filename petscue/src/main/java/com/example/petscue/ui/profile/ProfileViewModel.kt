package com.example.petscue.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.UserRole
import com.example.petscue.data.repository.MensajesRepository
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val viewedUserId: String? = savedStateHandle["userId"]

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var petsJob: Job? = null
    private var likedPostsJob: Job? = null
    private var postsJob: Job? = null

    private val _openChatEvent = MutableStateFlow<String?>(null)
    val openChatEvent: StateFlow<String?> = _openChatEvent.asStateFlow()

    init {
        refreshProfile()
    }

    fun openOrCreateGeneralChat() {
        viewModelScope.launch {
            val currentUser = repository.getCurrentUserProfile()
            val otherUserId = viewedUserId ?: return@launch

            if (currentUser.uid == otherUserId) return@launch

            val conversationId = mensajesRepository.createOrGetGeneralConversation(
                currentUserId = currentUser.uid,
                otherUserId = otherUserId
            )

            _openChatEvent.value = conversationId
        }
    }

    fun consumeOpenChatEvent() {
        _openChatEvent.value = null
    }

    fun onTabSelected(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            runCatching {
                val currentUser = repository.getCurrentUserProfile()

                val user = if (viewedUserId.isNullOrBlank() || viewedUserId == currentUser.uid) {
                    currentUser
                } else {
                    repository.getUserProfileById(viewedUserId)
                }

                val replies: List<Post> = runCatching {
                    repository.getRepliesByUser(user.uid)
                }.getOrDefault(emptyList())

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

                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        currentUserId = currentUser.uid,
                        user = user,
                        replies = replies,
                        followersCount = followersCount,
                        followingCount = followingCount,
                        isFollowing = isFollowing
                    )
                }

                postsJob?.cancel()
                postsJob = viewModelScope.launch {
                    repository.getPostsByUser(user.uid).collectLatest { posts ->
                        val sortedPosts = posts.sortedByDescending { it.timestamp }
                        _uiState.update { current ->
                            current.copy(
                                posts = sortedPosts,
                                mediaPosts = sortedPosts.filter { post -> post.fotos.isNotEmpty() }
                            )
                        }
                    }
                }

                likedPostsJob?.cancel()
                likedPostsJob = viewModelScope.launch {
                    repository.getLikedPostsByUser(user.uid).collectLatest { likedPosts ->
                        val sortedLikedPosts = likedPosts.sortedByDescending { it.timestamp }
                        _uiState.update { current ->
                            current.copy(
                                likedPosts = sortedLikedPosts
                            )
                        }
                    }
                }

                petsJob?.cancel()
                petsJob = viewModelScope.launch {
                    if (user.role == UserRole.PROTECTORA) {
                        repository.getAdoptionPetsByProtectora(user.uid).collectLatest { adoptionPets ->
                            _uiState.update { current ->
                                current.copy(
                                    pets = emptyList(),
                                    adoptionPets = adoptionPets
                                )
                            }
                        }
                    } else {
                        repository.getPetsByUser(user.uid).collectLatest { pets ->
                            _uiState.update { current ->
                                current.copy(
                                    pets = pets,
                                    adoptionPets = emptyList()
                                )
                            }
                        }
                    }
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(isLoading = false)
                }
            }
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            val state = _uiState.value
            val currentUserId = state.currentUserId
            val viewedUserId = state.user?.uid ?: return@launch

            if (currentUserId.isBlank() || viewedUserId.isBlank() || currentUserId == viewedUserId) {
                return@launch
            }

            if (state.isFollowing) {
                repository.unfollowUser(currentUserId, viewedUserId)
            } else {
                repository.followUser(currentUserId, viewedUserId)
            }

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
        super.onCleared()
        petsJob?.cancel()
        likedPostsJob?.cancel()
        postsJob?.cancel()
    }
}