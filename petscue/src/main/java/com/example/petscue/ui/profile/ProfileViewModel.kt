package com.example.petscue.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.UserRole
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val viewedUserId: String? = savedStateHandle["userId"]

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var petsJob: Job? = null

    init {
        refreshProfile()
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

                val posts = repository.getPostsByUser(user.uid)
                val mediaPosts = posts.filter { it.fotos.isNotEmpty() }

                val replies: List<Post> = runCatching {
                    repository.getRepliesByUser(user.uid)
                }.getOrDefault(emptyList())

                val likedPosts: List<Post> = runCatching {
                    repository.getLikedPostsByUser(user.uid)
                }.getOrDefault(emptyList())

                val followersCount = runCatching {
                    repository.getFollowersCount(user.uid)
                }.getOrDefault(0)

                val followingCount = runCatching {
                    repository.getFollowingCount(user.uid)
                }.getOrDefault(0)

                _uiState.update { current ->
                    current.copy(
                        isLoading = true,
                        currentUserId = currentUser.uid,
                        user = user,
                        posts = posts,
                        replies = replies,
                        mediaPosts = mediaPosts,
                        likedPosts = likedPosts,
                        followersCount = followersCount,
                        followingCount = followingCount
                    )
                }

                petsJob?.cancel()
                petsJob = viewModelScope.launch {
                    if (user.role == UserRole.PROTECTORA) {
                        repository.getAdoptionPetsByProtectora(user.uid).collectLatest { adoptionPets ->
                            _uiState.update { current ->
                                current.copy(
                                    isLoading = false,
                                    pets = emptyList(),
                                    adoptionPets = adoptionPets
                                )
                            }
                        }
                    } else {
                        repository.getPetsByUser(user.uid).collectLatest { pets ->
                            _uiState.update { current ->
                                current.copy(
                                    isLoading = false,
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
}