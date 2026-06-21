package com.example.petscue.data.repository

import com.example.petscue.data.model.AppNotification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun observeNotifications(): Flow<List<AppNotification>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users")
            .document(uid)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val items = snapshot?.documents.orEmpty().map { doc ->
                    AppNotification(
                        id = doc.id,
                        title = doc.getString("title").orEmpty(),
                        body = doc.getString("body").orEmpty(),
                        type = doc.getString("type").orEmpty(),
                        alertId = doc.getString("alertId").orEmpty(),
                        petId = doc.getString("petId").orEmpty(),
                        senderId = doc.getString("senderId").orEmpty(),
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                    )
                }
                trySend(items)
            }

        awaitClose { registration.remove() }
    }

    fun markAsRead(notificationId: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users")
            .document(uid)
            .collection("notifications")
            .document(notificationId)
            .update(
                mapOf(
                    "isRead" to true,
                    "readAt" to System.currentTimeMillis()
                )
            )
    }
}