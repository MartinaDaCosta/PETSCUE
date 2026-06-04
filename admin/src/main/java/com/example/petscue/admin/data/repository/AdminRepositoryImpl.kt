package com.example.petscue.admin.data.repository

import com.example.petscue.admin.data.model.ProtectoraRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) {

    fun observePendingRequests(): Flow<List<ProtectoraRequest>> = callbackFlow {
        val registration = db.collection("users")
            .whereEqualTo("role", "PROTECTORA")
            .whereEqualTo("approvalStatus", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.map { doc ->
                    ProtectoraRequest(
                        id = doc.id,
                        nombre = doc.getString("nombreProtectora").orEmpty(),
                        email = doc.getString("email").orEmpty(),
                        telefono = doc.getString("telefono").orEmpty(),
                        direccion = doc.getString("direccion").orEmpty(),
                        descripcion = doc.getString("descripcionProtectora").orEmpty(),
                        comunidad = "",
                        provincia = doc.getString("provincia").orEmpty(),
                        ciudad = doc.getString("ciudad").orEmpty(),
                        documentUrls = (doc.get("documentosUrls") as? List<*>)?.filterIsInstance<String>()
                            ?: emptyList(),
                        estado = doc.getString("approvalStatus").orEmpty(),
                        motivoRechazo = doc.getString("motivoRevision").orEmpty(),
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        reviewedAt = null
                    )
                } ?: emptyList()

                trySend(requests)
            }

        awaitClose { registration.remove() }
    }

    suspend fun approveRequest(requestId: String): Result<Unit> = runCatching {
        db.collection("users")
            .document(requestId)
            .update("approvalStatus", "APPROVED")
            .await()
    }

    suspend fun rejectRequest(requestId: String, motivo: String): Result<Unit> = runCatching {
        db.collection("users")
            .document(requestId)
            .update(
                mapOf(
                    "approvalStatus" to "REJECTED",
                    "motivoRevision" to motivo
                )
            )
            .await()
    }
}