package com.example.petscue.data.repository

import com.example.petscue.data.model.AvisoMapa
import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AlertRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AlertRepository {

    private val alertsRef = firestore.collection("alerts")
    private val petsRef = firestore.collection("pets")
    private val adoptionPetsRef = firestore.collection("adoption_pets")
    private val usersRef = firestore.collection("users")

    override fun getAllAlerts(): Flow<List<AvisoMapa>> = callbackFlow {
        val listener = alertsRef.addSnapshotListener { value, error ->
            if (error != null) {
                trySend(emptyList())
                close()
                return@addSnapshotListener
            }

            val alerts = value?.documents.orEmpty().mapNotNull { doc ->
                doc.toObject(AvisoMapa::class.java)?.copy(id = doc.id)
            }

            trySend(alerts)
        }

        awaitClose { listener.remove() }
    }

    override suspend fun getAlertByPetId(petId: String): AvisoMapa? {
        val doc = alertsRef.document(petId).get().await()
        return doc.toObject(AvisoMapa::class.java)?.copy(id = doc.id)
    }

    override suspend fun upsertAlert(alert: AvisoMapa) {
        val normalizedAlert = alert.copy(id = alert.petId)

        val petDoc = petsRef.document(alert.petId).get().await()
        val adoptionPetDoc = adoptionPetsRef.document(alert.petId).get().await()

        val targetPetRef = when {
            petDoc.exists() -> petsRef.document(alert.petId)
            adoptionPetDoc.exists() -> adoptionPetsRef.document(alert.petId)
            else -> throw IllegalStateException("No se encontró la mascota asociada al aviso.")
        }

        val newPetState = when (alert.tipoAviso.uppercase()) {
            "PERDIDO" -> "perdido"
            "ENCONTRADO" -> "encontrado"
            "VISTO" -> "visto"
            else -> "perdido"
        }

        val batch = firestore.batch()
        batch.set(alertsRef.document(alert.petId), normalizedAlert)
        batch.update(targetPetRef, "estado", newPetState)
        batch.commit().await()
    }

    override suspend fun deleteAlertByPetId(petId: String) {
        val existingAlert = alertsRef.document(petId).get().await()
        if (!existingAlert.exists()) return

        val alert = existingAlert.toObject(AvisoMapa::class.java)?.copy(id = existingAlert.id)
            ?: return

        val petDoc = petsRef.document(petId).get().await()
        val adoptionPetDoc = adoptionPetsRef.document(petId).get().await()

        val targetPetRef = when {
            petDoc.exists() -> petsRef.document(petId)
            adoptionPetDoc.exists() -> adoptionPetsRef.document(petId)
            else -> null
        }

        val ownerSnapshot = usersRef.document(alert.userId).get().await()
        val owner = ownerSnapshot.toObject(User::class.java)

        val fallbackState = when (owner?.role) {
            UserRole.PROTECTORA -> "en protectora"
            else -> "en casa"
        }

        val batch = firestore.batch()
        batch.delete(alertsRef.document(petId))

        targetPetRef?.let {
            batch.update(it, "estado", fallbackState)
        }

        batch.commit().await()
    }
}