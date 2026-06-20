package com.example.petscue.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.example.petscue.R
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = context.getString(R.string.default_notification_channel_id)
        val channelName = context.getString(R.string.default_notification_channel_name)

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones de avisos cercanos"
        }

        context.getSystemService<NotificationManager>()
            ?.createNotificationChannel(channel)
    }
}
fun saveCurrentFcmToken() {
    val user = FirebaseAuth.getInstance().currentUser
    Log.d("FCM", "currentUser uid = ${user?.uid}")

    val uid = user?.uid
    if (uid == null) {
        Log.e("FCM", "No hay usuario autenticado al guardar token")
        return
    }

    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            Log.d("FCM", "Token obtenido = $token")

            FirebaseFirestore.getInstance()
                .collection("fcmTokens")
                .document(uid)
                .set(
                    mapOf(
                        "token" to token,
                        "uid" to uid,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .addOnSuccessListener {
                    Log.d("FCM", "Token guardado en Firestore correctamente")
                }
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error guardando token en Firestore", e)
                }
        }
        .addOnFailureListener { e ->
            Log.e("FCM", "Error obteniendo token FCM", e)
        }
}