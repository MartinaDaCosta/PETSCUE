package com.example.petscue.data.repository

import com.example.petscue.data.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getAll(): Flow<List<Post>>
    suspend fun insert(post: Post, localImageUris: List<String> = emptyList())
    suspend fun delete(post: Post)
    suspend fun toggleLike(postId: String, userId: String)
    suspend fun getLikedPostsByUser(userId: String): List<Post>
}