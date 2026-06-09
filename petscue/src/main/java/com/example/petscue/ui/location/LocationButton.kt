package com.example.petscue.ui.location

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationButton(
    ubicacion: String,
    onUbicacionDetectada: (String, Double, Double) -> Unit
) {
    val context    = LocalContext.current
    val scope      = rememberCoroutineScope()
    var isLoading  by remember { mutableStateOf(false) }

    val locationPermission = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    Column {
        OutlinedTextField(
            value         = ubicacion,
            onValueChange = { },
            label         = { Text("Ubicación detectada") },
            readOnly      = true,
            leadingIcon   = {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.width(8.dp))

        OutlinedButton(
            onClick = {
                if (locationPermission.status.isGranted) {
                    scope.launch {
                        isLoading = true
                        val loc = getCurrentLocation(context)
                        if (loc != null) {
                            val texto = "%.4f, %.4f".format(loc.lat, loc.lng)
                            onUbicacionDetectada(texto, loc.lat, loc.lng)
                        }
                        isLoading = false
                    }
                } else {
                    locationPermission.launchPermissionRequest()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.width(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Detectar mi location")
            }
        }
    }
}