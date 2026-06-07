package com.example.petscue.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petscue.data.model.UserRole
import com.example.petscue.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun onTabSelected(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            runCatching {
                val user = repository.getCurrentUserProfile()
                val posts = repository.getPostsByUser(user.uid)

                val mediaPosts = posts.filter { it.fotos.isNotEmpty() }

                repository.getPetsByUser(user.uid).collectLatest { pets ->
                    val adoptionPets = if (user.role == UserRole.PROTECTORA) {
                        pets.filter { pet ->
                            pet.estado.contains("adop", ignoreCase = true)
                        }
                    } else {
                        emptyList()
                    }

                    _uiState.value = ProfileUiState(
                        isLoading = false,
                        user = user,
                        pets = pets,
                        posts = posts,
                        replies = emptyList(),
                        mediaPosts = mediaPosts,
                        likedPosts = emptyList(),
                        adoptionPets = adoptionPets,
                        followersCount = user.followers,
                        followingCount = user.following,
                        selectedTab = _uiState.value.selectedTab
                    )
                }
            }.onFailure {
                _uiState.update { state ->
                    state.copy(isLoading = false)
                }
            }
        }
    }
}