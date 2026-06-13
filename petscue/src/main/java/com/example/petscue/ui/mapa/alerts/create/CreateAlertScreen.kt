package com.example.petscue.ui.mapa.alerts.create

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petscue.data.model.Pet
import com.example.petscue.ui.novedades.location.SelectedLocation
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
import java.util.Locale
import kotlin.math.roundToInt

private val BluePrimary = Color(0xFF1976D2)
private val BlueDark = Color(0xFF0D47A1)
private val BlueSoft = Color(0xFFEFF6FF)
private val BlueBorder = Color(0xFFB9D8FF)
private val BgColor = Color(0xFFF8FBFF)
private val LostColor = Color(0xFFE53935)
private val FoundColor = Color(0xFF43A047)
private val SeenColor = Color(0xFFFB8C00)

@Composable
fun CreateAlertScreen(
    onBack: () -> Unit,
    onAlertSaved: () -> Unit,
    vm: CreateAlertViewModel = hiltViewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf(emptyList<SelectedLocation>()) }
    var loadingCurrentLocation by remember { mutableStateOf(false) }
    var resolvingMapAddress by remember { mutableStateOf(false) }

    val selectedLatLng = uiState.selectedLocation?.let {
        LatLng(it.lat, it.lng)
    } ?: LatLng(39.4699, -0.3763)

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

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onAlertSaved()
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
        color = BgColor
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }

            uiState.pet == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error ?: "No se encontró la mascota")
                }
            }

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
                        AlertPetCard(pet = uiState.pet!!)
                    }

                    item {
                        AlertTypeSection(
                            selectedType = uiState.alertType,
                            onTypeSelected = vm::onAlertTypeSelected
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { newQuery ->
                                query = newQuery

                                if (newQuery.length >= 2 && Places.isInitialized()) {
                                    val placesClient = Places.createClient(context)
                                    val request = FindAutocompletePredictionsRequest.builder()
                                        .setQuery(newQuery)
                                        .build()

                                    placesClient.findAutocompletePredictions(request)
                                        .addOnSuccessListener { response ->
                                            suggestions = response.autocompletePredictions.map {
                                                SelectedLocation(
                                                    address = it.getFullText(null).toString()
                                                )
                                            }
                                        }
                                        .addOnFailureListener {
                                            suggestions = emptyList()
                                        }
                                } else {
                                    suggestions = emptyList()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            placeholder = { Text("Buscar ubicación") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        locationPermissionLauncher.launch(
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                                    }
                                ) {
                                    if (loadingCurrentLocation) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = "Mi ubicación",
                                            tint = BluePrimary
                                        )
                                    }
                                }
                            }
                        )
                    }

                    if (suggestions.isNotEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BlueBorder)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 220.dp)
                                ) {
                                    items(suggestions) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    searchExactPlace(
                                                        context = context,
                                                        query = item.address,
                                                        onResult = { selected ->
                                                            vm.onLocationSelected(selected)
                                                            query = selected.address
                                                            suggestions = emptyList()
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
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(item.address)
                                        }
                                        HorizontalDivider(color = BlueBorder.copy(alpha = 0.35f))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BlueBorder)
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
                                    }
                                ) {
                                    uiState.selectedLocation?.let { selected ->
                                        val pos = LatLng(selected.lat, selected.lng)

                                        Marker(
                                            state = MarkerState(position = pos),
                                            title = selected.address
                                        )

                                        Circle(
                                            center = pos,
                                            radius = uiState.radiusMeters,
                                            fillColor = BluePrimary.copy(alpha = 0.12f),
                                            strokeColor = BluePrimary.copy(alpha = 0.45f),
                                            strokeWidth = 3f
                                        )
                                    }
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
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BlueBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Rango del aviso",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BlueDark
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = formatDistance(uiState.radiusMeters),
                                    color = BluePrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Slider(
                                    value = uiState.radiusMeters.toFloat(),
                                    onValueChange = {
                                        vm.onRadiusChanged(it.toDouble())
                                    },
                                    valueRange = 500f..10000f,
                                    steps = 18,
                                    colors = SliderDefaults.colors(
                                        thumbColor = BluePrimary,
                                        activeTrackColor = BluePrimary,
                                        inactiveTrackColor = BluePrimary.copy(alpha = 0.2f)
                                    )
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("500 m", color = Color.Gray)
                                    Text("10 km", color = Color.Gray)
                                }
                            }
                        }
                    }

                    uiState.error?.let { errorMessage ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1))
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = vm::saveAlert,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .height(54.dp),
                            enabled = !uiState.isSaving,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text("Guardar aviso")
                            }
                        }
                    }
                }
            }
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
                tint = BlueDark
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "Nuevo aviso",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BlueDark
            )
            Text(
                text = "Define el tipo, la ubicación y el rango",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B8DB8)
            )
        }
    }
}

@Composable
private fun AlertPetCard(pet: Pet) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val image = pet.fotos.firstOrNull()

            if (!image.isNullOrBlank()) {
                AsyncImage(
                    model = image,
                    contentDescription = pet.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Text(
                text = pet.nombre.ifBlank { "Mascota" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = BlueDark
            )

            Text(
                text = "${pet.especie.ifBlank { "-" }} · ${pet.raza.ifBlank { "-" }}",
                color = BluePrimary
            )

            Text(
                text = "${pet.genero.ifBlank { "-" }} · ${pet.edad.ifBlank { "-" }} · ${pet.peso.ifBlank { "-" }}",
                color = Color(0xFF6B8DB8)
            )

            if (pet.descripcion.isNotBlank()) {
                Text(
                    text = pet.descripcion,
                    color = Color(0xFF173A63)
                )
            }
        }
    }
}

@Composable
private fun AlertTypeSection(
    selectedType: AlertType,
    onTypeSelected: (AlertType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Tipo de aviso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BlueDark
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AlertTypeChip(
                    text = "Perdido",
                    color = LostColor,
                    selected = selectedType == AlertType.LOST,
                    onClick = { onTypeSelected(AlertType.LOST) },
                    modifier = Modifier.weight(1f)
                )
                AlertTypeChip(
                    text = "Encontrado",
                    color = FoundColor,
                    selected = selectedType == AlertType.FOUND,
                    onClick = { onTypeSelected(AlertType.FOUND) },
                    modifier = Modifier.weight(1f)
                )
                AlertTypeChip(
                    text = "Visto",
                    color = SeenColor,
                    selected = selectedType == AlertType.SEEN,
                    onClick = { onTypeSelected(AlertType.SEEN) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AlertTypeChip(
    text: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.18f) else Color.White
        ),
        border = BorderStroke(1.dp, if (selected) color else BlueBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
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