package com.example.petscue.domain.usecase

import android.annotation.SuppressLint
import android.content.Context
import com.example.petscue.data.model.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


/**
 * Recupera la ubicación actual del dispositivo usando alta precisión.
 * Devuelve null si no se puede obtener.
 */
@SuppressLint("MissingPermission")
suspend fun getCurrentLocation(context: Context): Location? {
    val client = LocationServices.getFusedLocationProviderClient(context)
    val cts = CancellationTokenSource()

    return suspendCancellableCoroutine { cont ->
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(
                        Location(
                            lat = location.latitude,
                            lng = location.longitude
                        )
                    )
                } else {
                    cont.resume(null)
                }
            }
            .addOnFailureListener {
                cont.resume(null)
            }

        cont.invokeOnCancellation {
            cts.cancel()
        }
    }
}