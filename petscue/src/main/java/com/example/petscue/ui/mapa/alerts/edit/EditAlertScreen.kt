package com.example.petscue.ui.mapa.alerts.edit

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petscue.BuildConfig
import com.example.petscue.ui.novedades.location.SelectedLocation
import com.example.petscue.ui.theme.AuthCardShape
import com.example.petscue.ui.theme.AuthTextFieldShape
import com.example.petscue.ui.theme.authPrimaryButtonColors
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EditAlertScreen(
    onBack: () -> Unit,
    onAlertUpdated: () -> Unit,
    vm: EditAlertViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(emptyList<SelectedLocation>()) }
    var loadingCurrentLocation by remember { mutableStateOf(false) }
    var resolvingMapAddress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!Places.isInitialized()) {
            Places.initialize(
                context.applicationContext,
                BuildConfig.MAPS_API_KEY,
                Locale.getDefault()
            )
        }
    }

    LaunchedEffect(uiState.direccionAviso) {
        if (uiState.direccionAviso.isNotBlank() && query != uiState.direccionAviso) {
            query = uiState.direccionAviso
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            onAlertUpdated()
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
                    vm.onLocationSelected(location)
                    query = location.address
                }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            uiState.isLoading -> LoadingState()
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Header(onBack = onBack)
                    }

                    item {
                        EditAlertPetCard(
                            nombreMascota = uiState.nombreMascota,
                            tipoAviso = uiState.tipoAviso,
                            sexo = uiState.sexo,
                            raza = uiState.raza,
                            edad = uiState.edad,
                            fotoUrl = uiState.fotoUrl
                        )
                    }

                    item {
                        LocationSearchField(
                            query = query,
                            loadingCurrentLocation = loadingCurrentLocation,
                            onQueryChange = { newQuery ->
                                query = newQuery
                                if (newQuery.length >= 2 && Places.isInitialized()) {
                                    loadPredictions(
                                        context = context,
                                        query = newQuery,
                                        onResult = { suggestions = it }
                                    )
                                } else {
                                    suggestions = emptyList()
                                }
                            },
                            onMyLocationClick = {
                                locationPermissionLauncher.launch(
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            }
                        )
                    }

                    if (suggestions.isNotEmpty()) {
                        item {
                            SuggestionsCard(
                                suggestions = suggestions,
                                onSuggestionClick = { suggestion ->
                                    searchExactPlace(
                                        context = context,
                                        query = suggestion.address,
                                        onResult = { selected ->
                                            vm.onLocationSelected(selected)
                                            query = selected.address
                                            suggestions = emptyList()
                                        }
                                    )
                                }
                            )
                        }
                    }

                    item {
                        AlertMapCard(
                            selectedLocation = uiState.selectedLocation,
                            radiusMeters = uiState.radiusMeters,
                            onMapClick = { latLng ->
                                resolvingMapAddress = true
                                getAddressFromLatLng(
                                    context = context,
                                    latLng = latLng,
                                    onResult = { address ->
                                        resolvingMapAddress = false
                                        val location = SelectedLocation(
                                            address = address,
                                            lat = latLng.latitude,
                                            lng = latLng.longitude
                                        )
                                        vm.onLocationSelected(location)
                                        query = address
                                    }
                                )
                            },
                            resolvingMapAddress = resolvingMapAddress
                        )
                    }

                    item {
                        RadiusCard(
                            radiusMeters = uiState.radiusMeters,
                            onRadiusChange = vm::onRadiusChanged
                        )
                    }

                    item {
                        DescriptionCard(
                            descripcion = uiState.descripcion,
                            onDescripcionChange = vm::onDescripcionChange
                        )
                    }


                    uiState.errorMessage?.let { message ->
                        item {
                            ErrorMessageCard(message)
                        }
                    }

                    item {
                        Button(
                            onClick = vm::save,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .height(54.dp),
                            enabled = !uiState.isSaving,
                            shape = AuthCardShape,
                            colors = authPrimaryButtonColors()
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Guardar cambios",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditAlertPetCard(
    nombreMascota: String,
    tipoAviso: String,
    sexo: String,
    raza: String,
    edad: String,
    fotoUrl: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (fotoUrl.isNotBlank()) {
                AsyncImage(
                    model = fotoUrl,
                    contentDescription = nombreMascota,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                text = nombreMascota.ifBlank { "Mascota" },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = tipoAviso.ifBlank { "AVISO" },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "${sexo.ifBlank { "-" }} · ${raza.ifBlank { "-" }} · ${edad.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DescriptionCard(
    descripcion: String,
    onDescripcionChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Descripción del aviso",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcionChange,
                modifier = Modifier.fillMaxWidth(),
                shape = AuthTextFieldShape,
                minLines = 4,
                placeholder = {
                    Text("Añade detalles útiles sobre la mascota o el aviso")
                }
            )
        }
    }
}


@Composable
private fun Header(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = "Editar aviso",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Actualiza ubicación, rango y detalles",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorMessageCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(14.dp)
        )
    }
}

@Composable
private fun LocationSearchField(
    query: String,
    loadingCurrentLocation: Boolean,
    onQueryChange: (String) -> Unit,
    onMyLocationClick: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        shape = AuthTextFieldShape,
        placeholder = {
            Text(
                text = "Buscar ubicación",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            IconButton(onClick = onMyLocationClick) {
                if (loadingCurrentLocation) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

@Composable
private fun SuggestionsCard(
    suggestions: List<SelectedLocation>,
    onSuggestionClick: (SelectedLocation) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        LazyColumn(
            modifier = Modifier.heightIn(max = 220.dp)
        ) {
            items(suggestions) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(item) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = item.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
            }
        }
    }
}

@Composable
private fun AlertMapCard(
    selectedLocation: SelectedLocation?,
    radiusMeters: Double,
    onMapClick: (LatLng) -> Unit,
    resolvingMapAddress: Boolean
) {
    val selectedLatLng = selectedLocation?.let {
        LatLng(it.lat, it.lng)
    } ?: LatLng(39.4699, -0.3763)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, 14f)
    }

    LaunchedEffect(selectedLatLng.latitude, selectedLatLng.longitude) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(selectedLatLng, 15f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = false),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),
                onMapClick = onMapClick
            ) {
                selectedLocation?.let { selected ->
                    val pos = LatLng(selected.lat, selected.lng)

                    Marker(
                        state = MarkerState(position = pos),
                        title = selected.address
                    )

                    Circle(
                        center = pos,
                        radius = radiusMeters,
                        fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        strokeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        strokeWidth = 3f
                    )
                }
            }

            if (resolvingMapAddress) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun RadiusCard(
    radiusMeters: Double,
    onRadiusChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Rango del aviso",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formatDistance(radiusMeters),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = radiusMeters.toFloat(),
                onValueChange = { value ->
                    onRadiusChange(value.toDouble())
                },
                valueRange = 500f..10000f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "500 m",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "10 km",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

private fun loadPredictions(
    context: Context,
    query: String,
    onResult: (List<SelectedLocation>) -> Unit
) {
    val placesClient = Places.createClient(context)
    val request = FindAutocompletePredictionsRequest.builder()
        .setQuery(query)
        .build()

    placesClient.findAutocompletePredictions(request)
        .addOnSuccessListener { response ->
            val results = response.autocompletePredictions.map { prediction ->
                SelectedLocation(
                    address = prediction.getFullText(null).toString()
                )
            }
            onResult(results)
        }
        .addOnFailureListener {
            onResult(emptyList())
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
            val prediction: AutocompletePrediction =
                predictionResponse.autocompletePredictions.firstOrNull()
                    ?: return@addOnSuccessListener

            fetchPlaceDetails(
                context = context,
                prediction = prediction,
                onResult = onResult
            )
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
                    address = place.address ?: place.name ?: "Ubicación",
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
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = addresses?.firstOrNull()?.getAddressLine(0)
                ?: "${latLng.latitude}, ${latLng.longitude}"
            onResult(address)
        }
    } catch (_: Exception) {
        onResult("${latLng.latitude}, ${latLng.longitude}")
    }
}

private fun formatDistance(meters: Double): String {
    return if (meters < 1000) {
        "${meters.roundToInt()} m"
    } else {
        String.format(Locale.getDefault(), "%.1f km", meters / 1000.0)
    }
}