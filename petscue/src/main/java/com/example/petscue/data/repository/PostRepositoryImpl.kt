package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
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

    override suspend fun insert(post: Post, localImageUris: List<String>) {
        val docRef = if (post.id.isBlank()) {
            postsRef.document()
        } else {
            postsRef.document(post.id)
        }

        val uploadedUrls = uploadImages(
            userId = post.userId,
            postId = docRef.id,
            imageUris = localImageUris.take(4)
        )

        val finalPost = post.copy(
            id = docRef.id,
            fotos = uploadedUrls
        )

        docRef.set(finalPost).await()
    }

    override suspend fun delete(post: Post) {
        post.fotos.forEach { imageUrl ->
            runCatching {
                storage.getReferenceFromUrl(imageUrl).delete().await()
            }
        }

        if (post.id.isNotBlank()) {
            postsRef.document(post.id).delete().await()
        }
    }

    private suspend fun uploadImages(
        userId: String,
        postId: String,
        imageUris: List<String>
    ): List<String> = coroutineScope {
        imageUris.mapIndexed { index, uriString ->
            async {
                val uri = Uri.parse(uriString)
                val imageRef = storage.reference
                    .child("posts")
                    .child(userId.ifBlank { "demo_user" })
                    .child(postId)
                    .child("image_${index}_${UUID.randomUUID()}.jpg")

                imageRef.putFile(uri).await()
                imageRef.downloadUrl.await().toString()
            }
        }.awaitAll()
    }
}