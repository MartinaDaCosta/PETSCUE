package com.example.petscue.data.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.petscue.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PetscueMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token: $token")

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Log.w("FCM", "No hay usuario logueado; token no guardado todavía")
            return
        }

        FirebaseFirestore.getInstance()
            .collection("fcmTokens")
            .document(uid)
            .set(mapOf("token" to token))
            .addOnSuccessListener {
                Log.d("FCM", "Nuevo token guardado en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error guardando nuevo token", e)
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Mensaje recibido: ${message.data}")

        val title = message.notification?.title ?: "Nuevo aviso cerca de ti"
        val body = message.notification?.body ?: "Tienes una nueva alerta"

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("FCM", "Sin permiso de notificaciones")
            return
        }

        val builder = NotificationCompat.Builder(this, "petscue_alerts")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(this).notify(
            System.currentTimeMillis().toInt(),
            builder.build()
        )
    }
}