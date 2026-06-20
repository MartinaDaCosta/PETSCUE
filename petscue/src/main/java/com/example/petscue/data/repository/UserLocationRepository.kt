package com.example.petscue.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    fun updateUserLocation(
        lat: Double,
        lng: Double,
        notificationsEnabled: Boolean = true,
        notificationRadius: Double = 1500.0
    ) {
        val uid = auth.currentUser?.uid ?: return

        val data = mapOf(
            "notificationsEnabled" to notificationsEnabled,
            "notificationRadius" to notificationRadius,
            "lastLocation" to mapOf(
                "lat" to lat,
                "lng" to lng
            ),
            "lastLocationUpdatedAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(uid)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
    }
}