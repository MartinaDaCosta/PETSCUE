package com.example.petscue.ui.profile.edit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.petscue.data.model.UserRole
import com.example.petscue.ui.theme.PetscueBlue
import com.example.petscue.ui.theme.PetscueBlueDark
import com.example.petscue.ui.theme.PetscueError
import com.example.petscue.ui.theme.PetscueLightSurfaceVariant
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onProfileUpdated: () -> Unit,
    vm: EditProfileViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            vm.onPhotoSelected(uri)
        }
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            onProfileUpdated()
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PetscueBlue
            )
        }
        return
    }

    val isProtectora = state.role == UserRole.PROTECTORA

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Editar perfil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = PetscueBlueDark
            )
        )

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfilePhotoEditor(
                isProtectora = isProtectora,
                nombre = state.nombre,
                nombreProtectora = state.nombreProtectora,
                currentPhotoUrl = state.currentPhotoUrl,
                selectedPhotoUri = state.selectedPhotoUri,
                onSelectPhoto = {
                    photoPicker.launch("image/*")
                }
            )

            EditSectionCard(
                title = if (isProtectora) {
                    "Datos de la protectora"
                } else {
                    "Datos personales"
                }
            ) {
                if (isProtectora) {
                    PetscueTextField(
                        value = state.nombreProtectora,
                        onValueChange = vm::onNombreProtectoraChange,
                        label = "Nombre de la protectora",
                        singleLine = true
                    )

                    PetscueTextField(
                        value = state.descripcionProtectora,
                        onValueChange = vm::onDescripcionChange,
                        label = "Descripción",
                        singleLine = false,
                        minLines = 4
                    )

                    PetscueTextField(
                        value = state.telefono,
                        onValueChange = vm::onTelefonoChange,
                        label = "Teléfono",
                        singleLine = true
                    )

                    PetscueTextField(
                        value = state.direccion,
                        onValueChange = vm::onDireccionChange,
                        label = "Dirección",
                        singleLine = true
                    )
                } else {
                    PetscueTextField(
                        value = state.nombre,
                        onValueChange = vm::onNombreChange,
                        label = "Nombre",
                        singleLine = true
                    )

                    PetscueTextField(
                        value = state.apellido,
                        onValueChange = vm::onApellidoChange,
                        label = "Apellidos",
                        singleLine = true
                    )

                    PetscueTextField(
                        value = state.telefono,
                        onValueChange = vm::onTelefonoChange,
                        label = "Teléfono",
                        singleLine = true
                    )

                    PetscueTextField(
                        value = state.direccion,
                        onValueChange = vm::onDireccionChange,
                        label = "Dirección",
                        singleLine = true
                    )
                }
            }

            if (isProtectora) {
                EditSectionCard(
                    title = "Redes y enlaces"
                ) {
                    PetscueTextField(
                        value = state.web,
                        onValueChange = vm::onWebChange,
                        label = "Página web",
                        singleLine = true
                    )

                    PetscueTextField(
                        value = state.instagram,
                        onValueChange = vm::onInstagramChange,
                        label = "Instagram",
                        singleLine = true
                    )

                    PetscueTextField(
                        value = state.facebook,
                        onValueChange = vm::onFacebookChange,
                        label = "Facebook",
                        singleLine = true
                    )
                }
            }

            state.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PetscueError.copy(alpha = 0.18f)
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = vm::saveProfile,
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PetscueBlue,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Guardar cambios",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ProfilePhotoEditor(
    isProtectora: Boolean,
    nombre: String,
    nombreProtectora: String,
    currentPhotoUrl: String,
    selectedPhotoUri: Uri?,
    onSelectPhoto: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val imageModel: Any? = selectedPhotoUri ?: currentPhotoUrl

        if (selectedPhotoUri != null || currentPhotoUrl.isNotBlank()) {
            AsyncImage(
                model = imageModel,
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(126.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .clip(CircleShape)
                    .background(PetscueLightSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                val initial = if (isProtectora) {
                    nombreProtectora.firstOrNull()
                } else {
                    nombre.firstOrNull()
                } ?: '?'

                Text(
                    text = initial.uppercase(),
                    color = PetscueBlue,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        IconButton(
            onClick = onSelectPhoto,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 96.dp)
                .clip(CircleShape)
                .background(PetscueBlue)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Cambiar foto",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun EditSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = PetscueBlueDark,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            content()
        }
    }
}

@Composable
private fun PetscueTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PetscueBlue,
            focusedLabelColor = PetscueBlue,
            cursorColor = PetscueBlue
        )
    )
}