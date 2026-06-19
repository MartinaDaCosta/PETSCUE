package com.example.petscue.ui.protectoras

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.example.petscue.data.model.User
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

@SuppressLint("MissingPermission")
fun getUserLocation(
    context: Context,
    onResult: (Location?) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        .addOnSuccessListener { location -> onResult(location) }
        .addOnFailureListener { onResult(null) }
}

fun geocodeProtectora(
    context: Context,
    protectora: User,
    onResult: (Double?, Double?) -> Unit
) {
    if (!Geocoder.isPresent()) {
        onResult(null, null)
        return
    }

    val query = listOf(
        protectora.nombreProtectora,
        protectora.direccion,
        protectora.ciudad,
        protectora.provincia,
        protectora.comunidad,
        "España"
    ).filter { it.isNotBlank() }.joinToString(", ")

    val geocoder = Geocoder(context, Locale.getDefault())

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<android.location.Address>) {
                val address = addresses.firstOrNull()
                onResult(address?.latitude, address?.longitude)
            }

            override fun onError(errorMessage: String?) {
                onResult(null, null)
            }
        })
    } else {
        try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 1)
            val address = addresses?.firstOrNull()
            onResult(address?.latitude, address?.longitude)
        } catch (_: Exception) {
            onResult(null, null)
        }
    }
}

fun calculateDistanceKm(
    userLat: Double,
    userLon: Double,
    protectoraLat: Double,
    protectoraLon: Double
): Float {
    val results = FloatArray(1)
    Location.distanceBetween(userLat, userLon, protectoraLat, protectoraLon, results)
    return results[0] / 1000f
}