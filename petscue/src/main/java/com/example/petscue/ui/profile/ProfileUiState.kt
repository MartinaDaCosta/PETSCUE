package com.example.petscue.ui.profile

import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.Reply
import com.example.petscue.data.model.User

enum class ProfileTab {
    PETS_OR_ADOPTION,
    POSTS,
    REPLIES,
    MEDIA,
    LIKES
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val currentUserId: String = "",
    val pets: List<Pet> = emptyList(),
    val posts: List<Post> = emptyList(),
    val replies: List<Reply> = emptyList(),
    val mediaPosts: List<Post> = emptyList(),
    val likedPosts: List<Post> = emptyList(),
    val adoptionPets: List<Pet> = emptyList(),
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowing: Boolean = false,
    val selectedTab: ProfileTab = ProfileTab.PETS_OR_ADOPTION,
    val error: String? = null,
    val likedReplies: List<Reply> = emptyList()
)