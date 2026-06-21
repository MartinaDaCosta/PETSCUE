package com.example.petscue.data.repository

import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getCurrentUserProfile(): User
    suspend fun getUserProfileById(userId: String): User
    suspend fun getPostsByUser(userId: String): List<Post>
    suspend fun getRepliesByUser(userId: String): List<Post>
    suspend fun getLikedPostsByUser(userId: String): List<Post>
    suspend fun getFollowersCount(userId: String): Int
    suspend fun getFollowingCount(userId: String): Int
    fun getPetsByUser(userId: String): Flow<List<Pet>>
    fun getAdoptionPetsByProtectora(userId: String): Flow<List<Pet>>
}