package com.example.petscue.data.repository

import com.example.petscue.data.model.User
import com.example.petscue.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ProtectorasRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ProtectorasRepository {

    override suspend fun getProtectoras(): List<User> {
        val snapshot = db.collection("users")
            .whereEqualTo("role", UserRole.PROTECTORA.name)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(User::class.java)?.copy(uid = doc.id)
        }
    }
}