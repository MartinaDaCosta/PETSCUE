package com.example.petscue.ui.protectoras

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petscue.ui.protectoras.getUserLocation

@Composable
fun ProtectorasScreenRoute(
    viewModel: ProtectorasViewModel = hiltViewModel(),
    onProtectoraClick: (String) -> Unit = {}
) {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getUserLocation(context) { location ->
                location?.let {
                    viewModel.setUserLocation(it.latitude, it.longitude)

                }
            }
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            getUserLocation(context) { location ->
                location?.let {
                    viewModel.setUserLocation(it.latitude, it.longitude)
                }
            }
        }
    }

    ProtectorasScreen(
        viewModel = viewModel,
        onProtectoraClick = onProtectoraClick
    )
}