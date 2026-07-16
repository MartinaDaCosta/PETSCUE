package com.example.petscue.ui.auth.signup

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petscue.BuildConfig
import com.example.petscue.data.model.UserRole
import com.example.petscue.domain.usecase.getCurrentLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

private val SignupBlueDark = Color(0xFF1565C0)
private val SignupBlueLight = Color(0xFF64B5F6)

private data class ResolvedAddressData(
    val fullAddress: String,
    val provincia: String,
    val ciudad: String
)

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    vm: SignupViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fetchingLocation by remember { mutableStateOf(false) }

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        vm.onProfileImageSelected(uri)
    }

    val verificationDocsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            vm.onVerificationDocumentsSelected(uris)
        }
    }

    LaunchedEffect(Unit) {
        if (!Places.isInitialized()) {
            Places.initialize(
                context.applicationContext,
                BuildConfig.MAPS_API_KEY,
                Locale.getDefault()
            )
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            val message = if (state.selectedRole == UserRole.PROTECTORA) {
                "Cuenta creada. Revisa tu correo para verificarla. Además, tu protectora quedará pendiente de validación."
            } else {
                "Cuenta creada correctamente. Revisa tu correo para verificar la cuenta."
            }
            snackbarHostState.showSnackbar(message = message)
            delay(1500)
            onSignupSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(SignupBlueLight, SignupBlueDark)
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crear Cuenta",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "¡Únete a Petscue!",
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            ProfileImagePicker(
                imageUri = state.selectedImageUri,
                onPickImage = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Tipo de cuenta",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = state.selectedRole == UserRole.USER,
                    onClick = { vm.onRoleSelected(UserRole.USER) },
                    label = { Text("Usuario") }
                )

                FilterChip(
                    selected = state.selectedRole == UserRole.PROTECTORA,
                    onClick = { vm.onRoleSelected(UserRole.PROTECTORA) },
                    label = { Text("Protectora") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AppField(
                value = state.nombre,
                onValueChange = vm::onNombreChange,
                label = "Nombre *"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppField(
                value = state.apellido,
                onValueChange = vm::onApellidoChange,
                label = "Apellido *"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppField(
                value = state.username,
                onValueChange = vm::onUsernameChange,
                label = "Nombre de usuario *"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppField(
                value = state.email,
                onValueChange = vm::onEmailChange,
                label = "Email *"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PasswordField(
                value = state.password,
                onValueChange = vm::onPasswordChange,
                visible = state.passwordVisible,
                onToggleVisibility = vm::onTogglePasswordVisibility
            )

            Spacer(modifier = Modifier.height(12.dp))

            AppField(
                value = state.telefono,
                onValueChange = vm::onTelefonoChange,
                label = "Teléfono"
            )

            Spacer(modifier = Modifier.height(12.dp))

            AddressAutocompleteSection(
                query = state.addressQuery,
                suggestions = state.addressSuggestions,
                onQueryChange = { query ->
                    vm.onAddressQueryChange(query)

                    if (query.length >= 2 && Places.isInitialized()) {
                        val placesClient = Places.createClient(context)
                        val request = FindAutocompletePredictionsRequest.builder()
                            .setQuery(query)
                            .build()

                        placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener { response ->
                                val items = response.autocompletePredictions.map {
                                    AddressSuggestion(
                                        id = it.placeId,
                                        title = it.getPrimaryText(null).toString(),
                                        subtitle = it.getSecondaryText(null).toString(),
                                        fullAddress = it.getFullText(null).toString()
                                    )
                                }
                                vm.onAddressSuggestionsLoaded(items)
                            }
                            .addOnFailureListener {
                                vm.onAddressSuggestionsLoaded(emptyList())
                            }
                    } else {
                        vm.onAddressSuggestionsLoaded(emptyList())
                    }
                },
                onSuggestionSelected = { suggestion ->
                    val placesClient = Places.createClient(context)

                    val request = FetchPlaceRequest.builder(
                        suggestion.id,
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
                            val latLng = place.latLng

                            scope.launch {
                                val resolved = if (latLng != null) {
                                    getResolvedAddressData(
                                        context = context,
                                        lat = latLng.latitude,
                                        lng = latLng.longitude
                                    )
                                } else {
                                    null
                                }

                                val finalAddress = place.address
                                    ?: resolved?.fullAddress
                                    ?: suggestion.fullAddress

                                vm.onResolvedLocationData(
                                    direccion = finalAddress,
                                    provincia = resolved?.provincia.orEmpty(),
                                    ciudad = resolved?.ciudad.orEmpty(),
                                    lat = latLng?.latitude,
                                    lng = latLng?.longitude
                                )
                            }
                        }
                        .addOnFailureListener {
                            vm.onAddressSuggestionSelected(suggestion)
                        }
                },
                onUseCurrentLocation = {
                    if (locationPermissionState.status.isGranted) {
                        scope.launch {
                            fetchingLocation = true

                            val loc = getCurrentLocation(context)

                            if (loc != null) {
                                val resolved = getResolvedAddressData(
                                    context = context,
                                    lat = loc.lat,
                                    lng = loc.lng
                                )

                                val textoFinal = resolved?.fullAddress
                                    .takeUnless { it.isNullOrBlank() }
                                    ?: loc.direccion.takeIf { it.isNotBlank() }
                                    ?: "Ubicación actual"

                                vm.onResolvedLocationData(
                                    direccion = textoFinal,
                                    provincia = resolved?.provincia.orEmpty(),
                                    ciudad = resolved?.ciudad.orEmpty(),
                                    lat = loc.lat,
                                    lng = loc.lng
                                )
                            } else {
                                snackbarHostState.showSnackbar("No se pudo obtener tu ubicación actual")
                            }

                            fetchingLocation = false
                        }
                    } else {
                        locationPermissionState.launchPermissionRequest()
                    }
                },
                loadingCurrentLocation = fetchingLocation
            )

            if (state.selectedRole == UserRole.PROTECTORA) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Datos de la protectora",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppField(
                    value = state.nombreProtectora,
                    onValueChange = vm::onNombreProtectoraChange,
                    label = "Nombre de la protectora *"
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppField(
                    value = state.provincia,
                    onValueChange = vm::onProvinciaChange,
                    label = "Provincia *"
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppField(
                    value = state.ciudad,
                    onValueChange = vm::onCiudadChange,
                    label = "Ciudad *"
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.descripcionProtectora,
                    onValueChange = vm::onDescripcionProtectoraChange,
                    label = { Text("Descripción", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = fieldColors()
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppField(
                    value = state.web,
                    onValueChange = vm::onWebChange,
                    label = "Web"
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppField(
                    value = state.facebook,
                    onValueChange = vm::onFacebookChange,
                    label = "Facebook"
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppField(
                    value = state.instagram,
                    onValueChange = vm::onInstagramChange,
                    label = "Instagram"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Documentos de verificación *",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        verificationDocsLauncher.launch(
                            arrayOf("application/pdf", "image/*")
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adjuntar uno o varios documentos")
                }

                if (state.verificationDocuments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.verificationDocuments.forEach { uri ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color.White
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = uri.lastPathSegment ?: "Documento adjunto",
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(onClick = { vm.removeVerificationDocument(uri) }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar documento",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.verificationNotes,
                    onValueChange = vm::onVerificationNotesChange,
                    label = { Text("Notas para la verificación", color = Color.White) },
                    placeholder = {
                        Text(
                            "Ej.: CIF, número de registro, web oficial, persona de contacto...",
                            color = Color.White.copy(alpha = 0.70f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = fieldColors()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFFFCDD2),
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "* campo obligatorio",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { vm.onRegisterClick() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = SignupBlueDark
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = SignupBlueDark
                    )
                } else {
                    Text(
                        text = "Crear Cuenta",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión",
                    color = Color.White
                )
            }

            if (state.selectedRole == UserRole.PROTECTORA) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Las cuentas de protectora requieren validación previa por parte del administrador.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AddressAutocompleteSection(
    query: String,
    suggestions: List<AddressSuggestion>,
    onQueryChange: (String) -> Unit,
    onSuggestionSelected: (AddressSuggestion) -> Unit,
    onUseCurrentLocation: () -> Unit,
    loadingCurrentLocation: Boolean
) {
    Text(
        text = "Dirección",
        color = Color.White,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Buscar dirección", color = Color.White.copy(alpha = 0.70f)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Color.White
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = fieldColors()
    )

    if (suggestions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 6.dp)
        ) {
            suggestions.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionSelected(item) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (item.subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.subtitle,
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                if (index < suggestions.lastIndex) {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = onUseCurrentLocation,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White
        )
    ) {
        if (loadingCurrentLocation) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Usar ubicación actual")
        }
    }
}

@Composable
private fun AppField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = fieldColors()
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Contraseña *", color = Color.White) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Mostrar u ocultar contraseña",
                    tint = Color.White
                )
            }
        },
        colors = fieldColors()
    )
}

@Composable
private fun ProfileImagePicker(
    imageUri: Any?,
    onPickImage: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Foto de perfil seleccionada",
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Foto de perfil",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onPickImage,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text("Elegir foto de perfil")
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.White.copy(alpha = 0.85f),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedPrefixColor = Color.White,
    unfocusedPrefixColor = Color.White.copy(alpha = 0.85f)
)

private suspend fun getResolvedAddressData(
    context: Context,
    lat: Double,
    lng: Double
): ResolvedAddressData? = suspendCancellableCoroutine { cont ->
    val geocoder = Geocoder(context, Locale.getDefault())

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(lat, lng, 1) { addresses ->
                val address = addresses.firstOrNull()
                cont.resume(
                    address?.let {
                        ResolvedAddressData(
                            fullAddress = it.toReadableText().orEmpty(),
                            provincia = it.adminArea.orEmpty(),
                            ciudad = it.locality.orEmpty()
                        )
                    }
                )
            }
        } else {
            @Suppress("DEPRECATION")
            val address = geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()
            cont.resume(
                address?.let {
                    ResolvedAddressData(
                        fullAddress = it.toReadableText().orEmpty(),
                        provincia = it.adminArea.orEmpty(),
                        ciudad = it.locality.orEmpty()
                    )
                }
            )
        }
    } catch (_: Exception) {
        cont.resume(null)
    }
}

private fun Address?.toReadableText(): String? {
    if (this == null) return null

    return listOfNotNull(
        thoroughfare,
        subThoroughfare,
        locality,
        adminArea,
        postalCode,
        countryName
    )
        .joinToString(", ")
        .replace("\\s+".toRegex(), " ")
        .trim()
        .ifBlank { getAddressLine(0) }
}