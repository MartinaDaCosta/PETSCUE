package com.example.petscue.ui.novedades.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale

private val BluePrimary = Color(0xFF4A90E2)
private val BlueSoft = Color(0xFFEAF3FF)
private val BlueBorder = Color(0xFF6CA9F0)

@Composable
fun LocationPickerScreen(
    onDismiss: () -> Unit,
    onLocationSelected: (SelectedLocation) -> Unit,
    viewModel: LocationPickerViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var loadingCurrentLocation by remember { mutableStateOf(false) }
    var resolvingMapAddress by remember { mutableStateOf(false) }

    val defaultLatLng = LatLng(39.4699, -0.3763)
    val selectedLatLng = uiState.selectedLocation?.let {
        LatLng(it.lat, it.lng)
    } ?: defaultLatLng

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, 14f)
    }

    LaunchedEffect(selectedLatLng.latitude, selectedLatLng.longitude) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLatLng, 15f)
    }

    LaunchedEffect(Unit) {
        if (!Places.isInitialized()) {
            Places.initialize(
                context.applicationContext,
                "AIzaSyCeWkMeZ-sZcAloA6rcyP9ZAKhmFFxMHd8",
                Locale.getDefault()
            )
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchCurrentLocation(
                context = context,
                onLoading = { loadingCurrentLocation = it },
                onResult = { location ->
                    viewModel.selectLocation(location)
                }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BlueSoft
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = BluePrimary
                    )
                }

                Text(
                    text = "Seleccionar ubicación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = {
                        uiState.selectedLocation?.let(onLocationSelected)
                    },
                    enabled = uiState.selectedLocation != null
                ) {
                    Text("USAR", fontWeight = FontWeight.Bold, color = BluePrimary)
                }
            }

            HorizontalDivider(color = BlueBorder)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { query ->
                            viewModel.updateQuery(query)

                            if (query.length >= 2 && Places.isInitialized()) {
                                val placesClient = Places.createClient(context)
                                val request = FindAutocompletePredictionsRequest.builder()
                                    .setQuery(query)
                                    .build()

                                placesClient.findAutocompletePredictions(request)
                                    .addOnSuccessListener { response ->
                                        viewModel.setSuggestions(
                                            response.autocompletePredictions.map {
                                                SelectedLocation(
                                                    address = it.getFullText(null).toString()
                                                )
                                            }
                                        )
                                    }
                                    .addOnFailureListener {
                                        viewModel.setSuggestions(emptyList())
                                    }
                            } else {
                                viewModel.setSuggestions(emptyList())
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar ubicación") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            ) {
                                if (loadingCurrentLocation) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Mi ubicación",
                                        tint = BluePrimary
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(18.dp)
                    )

                    if (uiState.suggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BlueBorder),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            LazyColumn(
                                modifier = Modifier.height(220.dp)
                            ) {
                                items(uiState.suggestions) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                searchExactPlace(
                                                    context = context,
                                                    query = item.address,
                                                    onResult = { selected ->
                                                        viewModel.selectLocation(selected)
                                                    }
                                                )
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = BluePrimary
                                        )
                                        Spacer(modifier = Modifier.size(12.dp))
                                        Text(item.address)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BlueBorder),
                        color = Color.White
                    ) {
                        Box {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(isMyLocationEnabled = false),
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = false,
                                    myLocationButtonEnabled = false
                                ),
                                onMapClick = { latLng ->
                                    resolvingMapAddress = true
                                    getAddressFromLatLng(
                                        context = context,
                                        latLng = latLng,
                                        onResult = { address ->
                                            resolvingMapAddress = false
                                            viewModel.selectLocation(
                                                SelectedLocation(
                                                    address = address,
                                                    lat = latLng.latitude,
                                                    lng = latLng.longitude
                                                )
                                            )
                                        }
                                    )
                                }
                            ) {
                                Marker(
                                    state = MarkerState(position = selectedLatLng),
                                    title = uiState.selectedLocation?.address ?: "Ubicación"
                                )
                            }

                            if (resolvingMapAddress) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = BluePrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, BlueBorder),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Ubicación seleccionada",
                                style = MaterialTheme.typography.labelLarge,
                                color = BluePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = uiState.selectedLocation?.address
                                    ?: "Pulsa en el mapa, usa tu ubicación o busca una dirección"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        uiState.selectedLocation?.let(onLocationSelected)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    enabled = uiState.selectedLocation != null,
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("CONFIRMAR UBICACIÓN")
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(
    context: Context,
    onLoading: (Boolean) -> Unit,
    onResult: (SelectedLocation) -> Unit
) {
    onLoading(true)
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    fusedClient.lastLocation
        .addOnSuccessListener { location ->
            if (location == null) {
                onLoading(false)
                return@addOnSuccessListener
            }

            val latLng = LatLng(location.latitude, location.longitude)
            getAddressFromLatLng(
                context = context,
                latLng = latLng,
                onResult = { address ->
                    onLoading(false)
                    onResult(
                        SelectedLocation(
                            address = address,
                            lat = latLng.latitude,
                            lng = latLng.longitude
                        )
                    )
                }
            )
        }
        .addOnFailureListener {
            onLoading(false)
        }
}

private fun searchExactPlace(
    context: Context,
    query: String,
    onResult: (SelectedLocation) -> Unit
) {
    val placesClient = Places.createClient(context)
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { predictionResponse ->
            val prediction = predictionResponse.autocompletePredictions.firstOrNull()
                ?: return@addOnSuccessListener
            fetchPlaceDetails(context, prediction, onResult)
        }
}

private fun fetchPlaceDetails(
    context: Context,
    prediction: AutocompletePrediction,
    onResult: (SelectedLocation) -> Unit
) {
    val placesClient = Places.createClient(context)
    val request = FetchPlaceRequest.builder(
        prediction.placeId,
        listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
        )
    ).build()

    placesClient.fetchPlace(request)
        .addOnSuccessListener { response ->
            val place = response.place
            val latLng = place.latLng ?: return@addOnSuccessListener

            onResult(
                SelectedLocation(
                    address = place.address ?: place.name ?: "${latLng.latitude}, ${latLng.longitude}",
                    lat = latLng.latitude,
                    lng = latLng.longitude
                )
            )
        }
}

private fun getAddressFromLatLng(
    context: Context,
    latLng: LatLng,
    onResult: (String) -> Unit
) {
    val geocoder = Geocoder(context, Locale.getDefault())

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(
                latLng.latitude,
                latLng.longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val address = addresses.firstOrNull()?.getAddressLine(0)
                            ?: "${latLng.latitude}, ${latLng.longitude}"
                        onResult(address)
                    }

                    override fun onError(errorMessage: String?) {
                        onResult("${latLng.latitude}, ${latLng.longitude}")
                    }
                }
            )
        } else {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = addresses?.firstOrNull()?.getAddressLine(0)
                ?: "${latLng.latitude}, ${latLng.longitude}"
            onResult(address)
        }
    } catch (_: Exception) {
        onResult("${latLng.latitude}, ${latLng.longitude}")
    }
}