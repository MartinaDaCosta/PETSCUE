package com.example.petscue.data.repository

import com.example.petscue.data.model.AvisoMapa
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditAlertRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getAlert(alertId: String): AvisoMapa? {
        val snapshot = firestore
            .collection("alerts")
            .document(alertId)
            .get()
            .await()

        return snapshot.toObject(AvisoMapa::class.java)?.copy(id = snapshot.id)
    }

    suspend fun updateAlert(alert: AvisoMapa) {
        firestore
            .collection("alerts")
            .document(alert.petId)
            .set(alert.copy(id = alert.petId))
            .await()
    }
}