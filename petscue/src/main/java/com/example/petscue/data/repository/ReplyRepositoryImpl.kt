package com.example.petscue.data.repository

import com.example.petscue.data.model.Reply
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReplyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReplyRepository {

    override fun getReplies(postId: String): Flow<List<Reply>> = callbackFlow {
        val listener = firestore.collection("posts")
            .document(postId)
            .collection("replies")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val replies = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Reply::class.java)?.copy(id = doc.id)
                }.orEmpty()

                trySend(replies)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun insertReply(postId: String, reply: Reply) {
        val repliesRef = firestore.collection("posts")
            .document(postId)
            .collection("replies")

        val docRef = if (reply.id.isBlank()) {
            repliesRef.document()
        } else {
            repliesRef.document(reply.id)
        }

        docRef.set(reply.copy(id = docRef.id, postId = postId)).await()
    }

    override suspend fun deleteReply(postId: String, replyId: String) {
        firestore.collection("posts")
            .document(postId)
            .collection("replies")
            .document(replyId)
            .delete()
            .await()
    }
}