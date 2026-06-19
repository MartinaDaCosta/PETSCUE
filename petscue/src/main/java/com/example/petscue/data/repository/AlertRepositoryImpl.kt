package com.example.petscue.data.repository

import com.example.petscue.data.model.AvisoMapa
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

    override fun getAllAlerts(): Flow<List<AvisoMapa>> = callbackFlow {
        val listener = alertsRef.addSnapshotListener { value, error ->
            if (error != null) {
                close(error)
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
        alertsRef.document(alert.petId).set(
            alert.copy(id = alert.petId)
        ).await()
    }

    override suspend fun deleteAlertByPetId(petId: String) {
        alertsRef.document(petId).delete().await()
    }
}