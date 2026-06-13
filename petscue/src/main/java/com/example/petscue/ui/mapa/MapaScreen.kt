package com.example.petscue.ui.mapa

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave

private val AzulPrimario = Color(0xFF1E88E5)
private val AzulBorde = Color(0xFF6CA9F0)
private val Blanco = Color.White
private val FondoPantalla = Color(0xFFF0F4FF)

data class LugarSugerido(
    val address: String,
    val lat: Double = 39.4699,
    val lng: Double = -0.3763,
    val placeId: String? = null
)

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapaScreen(
    viewModel: MapaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var vistaActiva by remember { mutableStateOf("MAPA") }
    var busqueda by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf(emptyList<LugarSugerido>()) }
    var lugarSeleccionado by remember { mutableStateOf<LugarSugerido?>(null) }
    var miUbicacion by remember { mutableStateOf<LatLng?>(null) }
    var mostrarSlider by remember { mutableStateOf(false) }

    val radioNotificaciones = uiState.radioNotificaciones
    val context = LocalContext.current
    val markerBitmaps = remember { mutableStateMapOf<String, Bitmap>() }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val defaultLatLng = LatLng(39.4950, -0.4060)

    val camaraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 13f)
    }

    val radarTransition = rememberInfiniteTransition(label = "radar")

    val radarScale by radarTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarScale"
    )

    val radarAlpha by radarTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )

    LaunchedEffect(Unit) {
        if (!Places.isInitialized()) {
            Places.initialize(
                context.applicationContext,
                "AIzaSyCeWkMeZ-sZcAloA6rcyP9ZAKhmFFxMHd8",
                Locale.getDefault()
            )
        }

        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            obtenerUbicacionActual(context) { latLng ->
                miUbicacion = latLng
                camaraState.position = CameraPosition.fromLatLngZoom(latLng, 14f)
            }
        }
    }

    LaunchedEffect(uiState.alerts) {
        uiState.alerts.forEach { aviso ->
            if (!markerBitmaps.containsKey(aviso.id)) {
                val bitmap = createMarkerBitmapFromUrl(
                    context = context,
                    imageUrl = aviso.fotoUrl,
                    nombre = aviso.nombreMascota,
                    color = colorTipoAviso(aviso.tipoAviso)
                )
                markerBitmaps[aviso.id] = bitmap
            }
        }
    }

    LaunchedEffect(lugarSeleccionado) {
        lugarSeleccionado?.let { lugar ->
            camaraState.position = CameraPosition.fromLatLngZoom(
                LatLng(lugar.lat, lugar.lng),
                16f
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoPantalla)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { query ->
                    busqueda = query
                    if (query.length >= 2 && Places.isInitialized()) {
                        val placesClient = Places.createClient(context)
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setQuery(query)
                            .build()

                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                sugerencias = response.autocompletePredictions.map {
                                    LugarSugerido(
                                        address = it.getFullText(null).toString(),
                                        placeId = it.placeId
                                    )
                                }
                            }
                            .addOnFailureListener {
                                sugerencias = emptyList()
                            }
                    } else {
                        sugerencias = emptyList()
                    }
                },
                placeholder = { Text("Buscar lugar", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = AzulPrimario
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF1A1A2E),
                    unfocusedTextColor = Color(0xFF1A1A2E),
                    cursorColor = AzulPrimario,
                    focusedBorderColor = AzulPrimario,
                    unfocusedBorderColor = AzulPrimario,
                    focusedContainerColor = Blanco,
                    unfocusedContainerColor = Blanco
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AzulPrimario)
                .height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("MAPA", "LISTA").forEach { opcion ->
                    val activo = vistaActiva == opcion
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (activo) Blanco else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = { vistaActiva = opcion },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = opcion,
                                color = if (activo) AzulPrimario else Blanco,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (vistaActiva == "MAPA") {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = camaraState,
                    properties = MapProperties(
                        isMyLocationEnabled = locationPermissionState.status.isGranted
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = true
                    )
                ) {
                    lugarSeleccionado?.let { lugar ->
                        Marker(
                            state = rememberMarkerState(
                                position = LatLng(lugar.lat, lugar.lng)
                            ),
                            title = ""
                        )
                    }

                    miUbicacion?.let { ubicacion ->
                        Circle(
                            center = ubicacion,
                            radius = radioNotificaciones,
                            fillColor = AzulPrimario.copy(alpha = 0.10f),
                            strokeColor = AzulPrimario.copy(alpha = 0.45f),
                            strokeWidth = 3f
                        )

                        Circle(
                            center = ubicacion,
                            radius = radioNotificaciones * radarScale.toDouble(),
                            fillColor = AzulPrimario.copy(alpha = radarAlpha),
                            strokeColor = AzulPrimario.copy(alpha = radarAlpha * 0.9f),
                            strokeWidth = 2f
                        )
                    }

                    uiState.alerts.forEach { aviso ->
                        val position = LatLng(aviso.lat, aviso.lng)
                        val zoom = camaraState.position.zoom
                        val markerBitmap = markerBitmaps[aviso.id]

                        when {
                            zoom >= 12.8f && markerBitmap != null -> {
                                Marker(
                                    state = rememberMarkerState(position = position),
                                    title = "",
                                    icon = BitmapDescriptorFactory.fromBitmap(markerBitmap)
                                )
                            }

                            zoom >= 10.8f -> {
                                MarkerComposable(
                                    state = rememberMarkerState(position = position),
                                    title = ""
                                ) {
                                    MarkerSoloNombre(
                                        nombre = aviso.nombreMascota,
                                        color = colorTipoAviso(aviso.tipoAviso)
                                    )
                                }
                            }

                            else -> {
                                MarkerComposable(
                                    state = rememberMarkerState(position = position),
                                    title = ""
                                ) {
                                    MarkerSoloPunto(
                                        color = colorTipoAviso(aviso.tipoAviso)
                                    )
                                }
                            }
                        }

                        Circle(
                            center = position,
                            radius = aviso.radioMetros,
                            fillColor = colorTipoAviso(aviso.tipoAviso).copy(alpha = 0.12f),
                            strokeColor = colorTipoAviso(aviso.tipoAviso).copy(alpha = 0.45f),
                            strokeWidth = 2f
                        )
                    }
                }

                if (sugerencias.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp, start = 4.dp, end = 4.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AzulBorde),
                        colors = CardDefaults.cardColors(containerColor = Blanco)
                    ) {
                        LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                            items(sugerencias) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val placeId = item.placeId ?: return@clickable
                                            fetchPlaceDetails(
                                                context = context,
                                                placeId = placeId,
                                                onResult = { seleccionado ->
                                                    lugarSeleccionado = seleccionado
                                                    busqueda = seleccionado.address
                                                    sugerencias = emptyList()
                                                }
                                            )
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = AzulPrimario
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = item.address,
                                        color = Color(0xFF1A1A2E),
                                        fontSize = 14.sp
                                    )
                                }

                                HorizontalDivider(color = AzulBorde.copy(alpha = 0.25f))
                            }
                        }
                    }
                }

                miUbicacion?.let {
                    val panelHeight = 108.dp
                    val tabWidth = 28.dp

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 74.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(panelHeight)
                                .align(Alignment.CenterStart)
                                .clickable { mostrarSlider = !mostrarSlider },
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                bottomStart = 0.dp,
                                topEnd = 14.dp,
                                bottomEnd = 14.dp
                            ),
                            color = AzulPrimario,
                            shadowElevation = 6.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (mostrarSlider) Icons.Default.ChevronLeft else Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Blanco
                                )
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = mostrarSlider,
                            modifier = Modifier
                                .padding(start = tabWidth + 6.dp)
                                .align(Alignment.CenterStart),
                            enter = slideInHorizontally(initialOffsetX = { -it / 2 }) +
                                    expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                            exit = slideOutHorizontally(targetOffsetX = { -it / 2 }) +
                                    shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                        ) {
                            Card(
                                modifier = Modifier
                                    .widthIn(max = 235.dp)
                                    .height(panelHeight),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Blanco),
                                border = BorderStroke(1.dp, AzulBorde.copy(alpha = 0.35f)),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Radio de notificaciones",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A1A2E),
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = formatoDistancia(radioNotificaciones),
                                            color = AzulPrimario,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Slider(
                                        value = radioNotificaciones.toFloat(),
                                        onValueChange = { viewModel.onRadioChanged(it.toDouble()) },
                                        valueRange = 500f..10000f,
                                        steps = 18,
                                        colors = SliderDefaults.colors(
                                            thumbColor = AzulPrimario,
                                            activeTrackColor = AzulPrimario,
                                            inactiveTrackColor = AzulPrimario.copy(alpha = 0.20f)
                                        )
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("500 m", color = Color.Gray, fontSize = 10.sp)
                                        Text("10 km", color = Color.Gray, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Blanco),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Vista Lista", color = AzulPrimario, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Aquí irá el listado de mascotas perdidas cerca de ti", color = Color.Gray, fontSize = 13.sp)
                }
            }
        }
    }
}

private suspend fun createMarkerBitmapFromUrl(
    context: Context,
    imageUrl: String,
    nombre: String,
    color: Color
): Bitmap = withContext(Dispatchers.IO) {
    val width = 250
    val height = 310
    val avatarSize = 124f
    val cornerRadius = 30f
    val borderStrokeWidth = 6f

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = android.graphics.Color.WHITE
    }

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.color = color.toArgb()
        this.strokeWidth = borderStrokeWidth
    }

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = "#1A1A2E".toColorInt()
        textSize = 34f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color.toArgb()
    }

    val avatarBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.color = color.copy(alpha = 0.25f).toArgb()
        this.strokeWidth = 4f
    }

    val cardRect = RectF(10f, 10f, width - 10f, 236f)
    canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, fillPaint)
    canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, strokePaint)

    val avatarCx = width / 2f
    val avatarCy = 86f
    val avatarRect = RectF(
        avatarCx - avatarSize / 2,
        avatarCy - avatarSize / 2,
        avatarCx + avatarSize / 2,
        avatarCy + avatarSize / 2
    )

    if (imageUrl.isNotBlank()) {
        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .build()

        val result = imageLoader.execute(request)

        if (result is SuccessResult) {
            val drawable = result.drawable
            val srcBitmap = createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1)
            )
            val srcCanvas = Canvas(srcBitmap)
            drawable.setBounds(0, 0, srcCanvas.width, srcCanvas.height)
            drawable.draw(srcCanvas)

            val scaled = srcBitmap.scale(
                avatarSize.toInt(),
                avatarSize.toInt(),
                true
            )

            val clipPath = Path().apply {
                addOval(avatarRect, Path.Direction.CW)
            }

            canvas.withSave {
                clipPath(clipPath)
                drawBitmap(scaled, avatarRect.left, avatarRect.top, null)
            }

            canvas.drawOval(avatarRect, avatarBorderPaint)
        } else {
            drawAvatarFallback(canvas, avatarRect, nombre, color, avatarBorderPaint)
        }
    } else {
        drawAvatarFallback(canvas, avatarRect, nombre, color, avatarBorderPaint)
    }

    val safeName = nombre.take(18)
    canvas.drawText(safeName, width / 2f, 200f, textPaint)

    val pinCx = width / 2f
    val pinCy = 254f
    canvas.drawCircle(pinCx, pinCy, 20f, pinPaint)

    val innerPinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(pinCx, pinCy, 8f, innerPinPaint)

    bitmap
}

private fun drawAvatarFallback(
    canvas: Canvas,
    avatarRect: RectF,
    nombre: String,
    color: Color,
    avatarBorderPaint: Paint
) {
    val fallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        this.color = color.copy(alpha = 0.15f).toArgb()
    }

    val fallbackTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color.toArgb()
        textSize = 34f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    val cx = avatarRect.centerX()
    val cy = avatarRect.centerY()
    canvas.drawOval(avatarRect, fallbackPaint)
    canvas.drawOval(avatarRect, avatarBorderPaint)

    val letter = nombre.take(1).uppercase()
    val textY = cy - ((fallbackTextPaint.descent() + fallbackTextPaint.ascent()) / 2)
    canvas.drawText(letter, cx, textY, fallbackTextPaint)
}

@SuppressLint("MissingPermission")
private fun obtenerUbicacionActual(
    context: Context,
    onResult: (LatLng) -> Unit
) {
    LocationServices.getFusedLocationProviderClient(context)
        .lastLocation
        .addOnSuccessListener { location ->
            if (location != null) {
                onResult(LatLng(location.latitude, location.longitude))
            }
        }
}

private fun fetchPlaceDetails(
    context: Context,
    placeId: String,
    onResult: (LugarSugerido) -> Unit
) {
    val placesClient = Places.createClient(context)

    val request = FetchPlaceRequest.builder(
        placeId,
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
                LugarSugerido(
                    address = place.address ?: place.name ?: "Ubicación",
                    lat = latLng.latitude,
                    lng = latLng.longitude,
                    placeId = place.id
                )
            )
        }
}

private fun formatoDistancia(metros: Double): String {
    return if (metros < 1000) {
        "${metros.roundToInt()} m"
    } else {
        String.format(Locale.getDefault(), "%.1f km", metros / 1000.0)
    }
}

