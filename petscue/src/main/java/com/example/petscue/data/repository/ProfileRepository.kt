package com.example.petscue.data.repository

import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.User

interface ProfileRepository {
    suspend fun getCurrentUserProfile(): User
    suspend fun getPetsByUser(userId: String): List<Pet>
    suspend fun getPostsByUser(userId: String): List<Post>
}