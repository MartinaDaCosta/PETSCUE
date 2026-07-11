package com.example.petscue.data.repository

import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ProfileRepository {

    override suspend fun getCurrentUserProfile(): User {
        val uid = auth.currentUser?.uid
            ?: error("No hay sesión iniciada.")

        return getUserProfileById(uid)
    }

    override suspend fun getUserProfileById(userId: String): User {
        val snapshot = db.collection("users")
            .document(userId)
            .get()
            .await()

        return snapshot.toObject(User::class.java)
            ?: error("No se pudo cargar el perfil.")
    }

    override fun getPetsByUser(userId: String): Flow<List<Pet>> = callbackFlow {
        val listener = db.collection("pets")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val pets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pet::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(pets).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override fun getPostsByUser(userId: String): Flow<List<Post>> = callbackFlow {
        val listener = db.collection("posts")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(posts).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getRepliesByUser(userId: String): List<Post> {
        return emptyList()
    }

    override fun getLikedPostsByUser(userId: String): Flow<List<Post>> = callbackFlow {
        val listener = db.collection("posts")
            .whereArrayContains("likedBy", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val likedPosts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Post::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(likedPosts).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override suspend fun getFollowersCount(userId: String): Int {
        val snapshot = db.collection("follows")
            .whereEqualTo("followedId", userId)
            .count()
            .get(AggregateSource.SERVER)
            .await()

        return snapshot.count.toInt()
    }

    override suspend fun getFollowingCount(userId: String): Int {
        val snapshot = db.collection("follows")
            .whereEqualTo("followerId", userId)
            .count()
            .get(AggregateSource.SERVER)
            .await()

        return snapshot.count.toInt()
    }

    override fun getAdoptionPetsByProtectora(protectoraId: String): Flow<List<Pet>> = callbackFlow {
        val listener = db.collection("adoption_pets")
            .whereEqualTo("userId", protectoraId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val adoptionPets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pet::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(adoptionPets).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override suspend fun isFollowing(followerId: String, followedId: String): Boolean {
        val snapshot = db.collection("follows")
            .whereEqualTo("followerId", followerId)
            .whereEqualTo("followedId", followedId)
            .get()
            .await()

        return !snapshot.isEmpty
    }

    override suspend fun followUser(followerId: String, followedId: String) {
        if (followerId == followedId) return

        val existing = db.collection("follows")
            .whereEqualTo("followerId", followerId)
            .whereEqualTo("followedId", followedId)
            .get()
            .await()

        if (existing.isEmpty) {
            val followId = "${followerId}_$followedId"
            db.collection("follows")
                .document(followId)
                .set(
                    mapOf(
                        "followerId" to followerId,
                        "followedId" to followedId,
                        "createdAt" to System.currentTimeMillis()
                    )
                )
                .await()
        }
    }

    override suspend fun unfollowUser(followerId: String, followedId: String) {
        val snapshot = db.collection("follows")
            .whereEqualTo("followerId", followerId)
            .whereEqualTo("followedId", followedId)
            .get()
            .await()

        snapshot.documents.forEach { doc ->
            doc.reference.delete().await()
        }
    }
}