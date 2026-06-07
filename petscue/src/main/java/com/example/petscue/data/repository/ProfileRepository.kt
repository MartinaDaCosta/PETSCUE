package com.example.petscue.data.repository

import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getCurrentUserProfile(): User
    fun getPetsByUser(userId: String): Flow<List<Pet>>
    suspend fun getPostsByUser(userId: String): List<Post>
}