// admin/data/repository/AdminRepositoryImpl.kt
package com.example.petscue.admin.data.repository

import com.example.petscue.admin.data.model.ProtectoraDocument
import com.example.petscue.admin.data.model.ProtectoraRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : AdminRepository {

    override fun observePendingRequests(): Flow<List<ProtectoraRequest>> = callbackFlow {
        val registration = db.collection("users")
            .whereEqualTo("role", "PROTECTORA")
            .whereEqualTo("approvalStatus", "PENDING")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    close()
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents
                    ?.map { document ->
                        document.toProtectoraRequest()
                    }
                    .orEmpty()

                trySend(requests)
            }

        awaitClose {
            registration.remove()
        }
    }

    override suspend fun approveRequest(
        requestId: String
    ): Result<Unit> = runCatching {
        db.collection("users")
            .document(requestId)
            .update(
                mapOf(
                    "approvalStatus" to "APPROVED",
                    "motivoRevision" to "",
                    "reviewedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    override suspend fun rejectRequest(
        requestId: String,
        motivo: String
    ): Result<Unit> = runCatching {
        require(motivo.trim().isNotBlank()) {
            "Debes indicar el motivo del rechazo."
        }

        db.collection("users")
            .document(requestId)
            .update(
                mapOf(
                    "approvalStatus" to "REJECTED",
                    "motivoRevision" to motivo.trim(),
                    "reviewedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    private fun DocumentSnapshot.toProtectoraRequest(): ProtectoraRequest {
        return ProtectoraRequest(
            id = id,
            nombre = getString("nombreProtectora").orEmpty(),
            email = getString("email").orEmpty(),
            telefono = getString("telefono").orEmpty(),
            direccion = getString("direccion").orEmpty(),
            descripcion = getString("descripcionProtectora").orEmpty(),
            comunidad = getString("comunidad").orEmpty(),
            provincia = getString("provincia").orEmpty(),
            ciudad = getString("ciudad").orEmpty(),
            documentos = get("documentos").toProtectoraDocuments(),
            estado = getString("approvalStatus").orEmpty(),
            motivoRechazo = getString("motivoRevision").orEmpty(),
            createdAt = getLong("createdAt") ?: 0L,
            reviewedAt = getLong("reviewedAt")
        )
    }

    private fun Any?.toProtectoraDocuments(): List<ProtectoraDocument> {
        val rawDocuments = this as? List<*> ?: return emptyList()

        return rawDocuments.mapNotNull { item ->
            val documentMap = item as? Map<*, *> ?: return@mapNotNull null

            val url = documentMap["url"] as? String
                ?: return@mapNotNull null

            ProtectoraDocument(
                name = documentMap["name"] as? String ?: "Documento",
                url = url
            )
        }
    }
}