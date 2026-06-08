package com.example.petscue.data.repository

import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.Post
import com.example.petscue.data.model.User
import com.google.firebase.auth.FirebaseAuth
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

        val snapshot = db.collection("users")
            .document(uid)
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

    override suspend fun getPostsByUser(userId: String): List<Post> {
        val snapshot = db.collection("posts")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Post::class.java)?.copy(id = doc.id)
        }
    }

    override suspend fun getRepliesByUser(userId: String): List<Post> {
        return emptyList()
    }

    override suspend fun getLikedPostsByUser(userId: String): List<Post> {
        return emptyList()
    }

    override suspend fun getFollowersCount(userId: String): Int {
        return 0
    }

    override suspend fun getFollowingCount(userId: String): Int {
        return 0
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
}