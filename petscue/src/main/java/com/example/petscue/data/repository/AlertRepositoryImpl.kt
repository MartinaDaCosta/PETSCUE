package com.example.petscue.data.repository

import android.net.Uri
import com.example.petscue.data.model.AvisoMapa
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AlertRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AlertRepository {

    private val alertsRef = firestore.collection("alerts")

    override suspend fun insertAlert(alert: AvisoMapa, imageUri: Uri?): Result<Unit> {
        return runCatching {
            val doc = if (alert.id.isBlank()) alertsRef.document() else alertsRef.document(alert.id)
            val alertId = doc.id

            val finalImageUrl = if (imageUri != null) {
                val imageRef = storage.reference.child("alerts/$alertId/photo.jpg")
                imageRef.putFile(imageUri).await()
                imageRef.downloadUrl.await().toString()
            } else {
                alert.fotoUrl
            }

            doc.set(
                alert.copy(
                    id = alertId,
                    fotoUrl = finalImageUrl
                )
            ).await()
        }
    }

    override fun getAllAlerts(): Flow<List<AvisoMapa>> = callbackFlow {
        val listener = alertsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val alerts = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(AvisoMapa::class.java)?.copy(id = doc.id)
            }.orEmpty()

            trySend(alerts).isSuccess
        }

        awaitClose { listener.remove() }
    }
}