package com.example.petscue.data.repository

import com.example.petscue.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PostRepository {

    private val postsRef = firestore.collection("posts")

    override fun getAll(): Flow<List<Post>> = callbackFlow {
        val listener = postsRef
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(posts)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun insert(post: Post) {
        val docRef = if (post.id.isBlank()) {
            postsRef.document()
        } else {
            postsRef.document(post.id)
        }

        docRef.set(post.copy(id = docRef.id)).await()
    }

    override suspend fun delete(post: Post) {
        if (post.id.isNotBlank()) {
            postsRef.document(post.id).delete().await()
        }
    }
}