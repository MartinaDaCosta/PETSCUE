package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.Pet
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject



class PetRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : PetRepository {

    private val petsRef = firestore.collection("pets")
    private val adoptionPetsRef = firestore.collection("adoption_pets")

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

                trySend(pets).isSuccess
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

                trySend(pets).isSuccess
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

                trySend(pets).isSuccess
            }

        awaitClose { listener.remove() }
    }

    override fun getAdoptionPetsByUserId(userId: String): Flow<List<Pet>> = callbackFlow {
        val listener = adoptionPetsRef
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

    override suspend fun insert(pet: Pet) {
        val uid = auth.currentUser?.uid
            ?: error("No hay sesión iniciada.")

        val userSnapshot = firestore.collection("users")
            .document(uid)
            .get()
            .await()

        val currentUser = userSnapshot.toObject(User::class.java)
            ?: error("No se pudo cargar el usuario.")

        val targetCollection = if (currentUser.role == UserRole.PROTECTORA) {
            adoptionPetsRef
        } else {
            petsRef
        }

        val docRef = if (pet.id.isBlank()) {
            targetCollection.document()
        } else {
            targetCollection.document(pet.id)
        }

        val petToSave = pet.copy(
            id = docRef.id,
            userId = uid
        )

        docRef.set(petToSave).await()
    }

    override suspend fun delete(pet: Pet) {
        if (pet.id.isBlank()) return

        runCatching { petsRef.document(pet.id).delete().await() }
        runCatching { adoptionPetsRef.document(pet.id).delete().await() }
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

    override suspend fun getAdoptionPetById(petId: String): Pet? {
        val snapshot = adoptionPetsRef.document(petId).get().await()
        return snapshot.toObject(Pet::class.java)?.copy(id = snapshot.id)
    }

    override suspend fun updateAdoptionPet(pet: Pet) {
        require(pet.id.isNotBlank()) { "El id de la mascota no puede estar vacío." }
        require(pet.userId.isNotBlank()) { "El userId de la mascota no puede estar vacío." }

        val currentUid = auth.currentUser?.uid
            ?: error("No hay sesión iniciada.")

        if (currentUid != pet.userId) {
            error("No tienes permisos para editar esta mascota.")
        }

        val data = mapOf(
            "nombre" to pet.nombre,
            "especie" to pet.especie,
            "raza" to pet.raza,
            "genero" to pet.genero,
            "edad" to pet.edad,
            "peso" to pet.peso,
            "descripcion" to pet.descripcion,
            "ubicacion" to pet.ubicacion,
            "estado" to pet.estado,
            "fotos" to pet.fotos,
            "userId" to pet.userId
        )

        adoptionPetsRef
            .document(pet.id)
            .update(data)
            .await()
    }

    override suspend fun getPetById(petId: String): Pet? {
        val snapshot = petsRef.document(petId).get().await()
        return snapshot.toObject(Pet::class.java)?.copy(id = snapshot.id)
    }

    override suspend fun getAnyPetById(petId: String): Pet? {
        return getPetById(petId) ?: getAdoptionPetById(petId)
    }

    override fun getAlertPetsByCurrentUser(): Flow<List<Pet>> = flow {
        val uid = auth.currentUser?.uid ?: error("No hay sesión iniciada.")

        val userSnapshot = firestore.collection("users")
            .document(uid)
            .get()
            .await()

        val currentUser = userSnapshot.toObject(User::class.java)
            ?: error("No se pudo cargar el usuario.")

        if (currentUser.role == UserRole.PROTECTORA) {
            emitAll(getAdoptionPetsByUserId(uid))
        } else {
            emitAll(getByUserId(uid))
        }
    }
}