package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.Pet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : PetRepository {

    private val petsRef = firestore.collection("pets")

    override fun getAll(): Flow<List<Pet>> = callbackFlow {
        val listener = petsRef
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val pets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pet::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(pets)
            }

        awaitClose { listener.remove() }
    }

    override fun getByEstado(estado: String): Flow<List<Pet>> = callbackFlow {
        val listener = petsRef
            .whereEqualTo("estado", estado)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val pets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pet::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(pets)
            }

        awaitClose { listener.remove() }
    }

    override fun getByUserId(userId: String): Flow<List<Pet>> = callbackFlow {
        val listener = petsRef
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val pets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pet::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(pets)
            }

        awaitClose { listener.remove() }
    }

    override fun getAdoptionPetsByUserId(userId: String): Flow<List<Pet>> = callbackFlow {
        val listener = petsRef
            .whereEqualTo("userId", userId)
            .whereEqualTo("estado", "en adopción")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val pets = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pet::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(pets)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun insert(pet: Pet) {
        val docRef = if (pet.id.isBlank()) {
            petsRef.document()
        } else {
            petsRef.document(pet.id)
        }

        docRef.set(pet.copy(id = docRef.id)).await()
    }

    override suspend fun delete(pet: Pet) {
        if (pet.id.isNotBlank()) {
            petsRef.document(pet.id).delete().await()
        }
    }

    override suspend fun uploadPetImages(
        petId: String,
        imageUris: List<Uri>
    ): List<String> {
        if (imageUris.isEmpty()) return emptyList()

        return imageUris.mapIndexed { index, uri ->
            val imageRef = storage.reference
                .child("pets")
                .child(petId)
                .child("${index}_${UUID.randomUUID()}.jpg")

            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        }
    }
}