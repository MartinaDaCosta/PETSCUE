package com.example.petscue.data.repository

import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.Reply
import com.example.petscue.data.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getCurrentUserProfile(): User
    suspend fun getUserProfileById(userId: String): User

    fun getPetsByUser(userId: String): Flow<List<Pet>>
    fun getAdoptionPetsByProtectora(protectoraId: String): Flow<List<Pet>>

    fun getPostsByUser(userId: String): Flow<List<Post>>
    fun getLikedPostsByUser(userId: String): Flow<List<Post>>
    fun getRepliesByUser(userId: String): Flow<List<Reply>>
    fun getRepostedPostsByUser(userId: String): Flow<List<Post>>
    suspend fun getFollowersCount(userId: String): Int
    suspend fun getFollowingCount(userId: String): Int


    suspend fun isFollowing(
        followerId: String,
        followedId: String
    ): Boolean

    suspend fun followUser(
        followerId: String,
        followedId: String
    )

    suspend fun unfollowUser(
        followerId: String,
        followedId: String
    )
}