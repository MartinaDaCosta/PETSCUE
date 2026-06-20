package com.example.petscue.notifications

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

@Composable
fun NotificationsBootstrap() {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                fetchAndStoreToken()
            }
        }
    )

    LaunchedEffect(Unit) {
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED

        if (needsPermission) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            fetchAndStoreToken()
        }
    }
}

private fun fetchAndStoreToken() {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            val data = mapOf(
                "token" to token,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            FirebaseFirestore.getInstance()
                .collection("fcmTokens")
                .document(uid)
                .set(data)
        }
}