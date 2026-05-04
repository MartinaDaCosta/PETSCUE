package com.example.petscue.data.repository

import com.example.petscue.data.model.Post
import com.example.petscue.data.sources.local.PostDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao
) : PostRepository {
    override fun getAll(): Flow<List<Post>> = postDao.getAll()
    override suspend fun insert(post: Post) = postDao.insert(post)
    override suspend fun delete(post: Post) = postDao.delete(post)
}