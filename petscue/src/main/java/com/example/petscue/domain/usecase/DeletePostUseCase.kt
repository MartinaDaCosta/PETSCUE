package com.example.petscue.domain.usecase

import com.example.petscue.data.model.Post
import com.example.petscue.data.repository.PostRepository
import javax.inject.Inject

class DeletePostUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(post: Post) {
        repository.delete(post)
    }
}