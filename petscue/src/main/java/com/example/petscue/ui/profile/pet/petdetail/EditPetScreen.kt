package com.example.petscue.ui.profile.pet.petdetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage

private val especies = listOf(
    "Perro",
    "Gato"
)

private val generos = listOf(
    "Macho",
    "Hembra"
)

private val razasPerro = listOf(
    "Labrador Retriever",
    "Golden Retriever",
    "Pastor Alemán",
    "Bulldog Francés",
    "Caniche",
    "Chihuahua",
    "Border Collie",
    "Beagle",
    "Yorkshire Terrier",
    "Mestizo"
)

private val razasGato = listOf(
    "Europeo Común",
    "Siamés",
    "Persa",
    "Maine Coon",
    "Bengalí",
    "British Shorthair",
    "Ragdoll",
    "Sphynx",
    "Azul Ruso",
    "Mestizo"
)

@Composable
fun EditPetScreen(
    onBack: () -> Unit,
    onPetUpdated: () -> Unit,
    vm: EditPetViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    val photosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        vm.onPhotosSelected(uris)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onPetUpdated()
        }
    }

    val razasDisponibles = when (state.especie) {
        "Perro" -> razasPerro
        "Gato" -> razasGato
        else -> emptyList()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    EditPetHeader(
                        onBack = onBack
                    )
                }

                item {
                    EditPhotosSection(
                        currentPhotoUrls = state.currentPhotoUrls,
                        newPhotoUris = state.newPhotoUris,
                        onAddPhotos = {
                            photosLauncher.launch("image/*")
                        },
                        onRemoveExisting = vm::removeExistingPhoto,
                        onRemoveNew = vm::removeNewPhoto
                    )
                }

                item {
                    FormSection(
                        title = "Información básica"
                    ) {
                        AppTextField(
                            value = state.nombre,
                            onValueChange = vm::onNombreChange,
                            label = "Nombre",
                            singleLine = true
                        )

                        AppDropdownField(
                            label = "Especie",
                            selectedValue = state.especie,
                            options = especies,
                            onValueSelected = vm::onEspecieChange
                        )

                        AppDropdownField(
                            label = "Raza",
                            selectedValue = state.raza,
                            options = razasDisponibles,
                            enabled = state.especie.isNotBlank(),
                            onValueSelected = vm::onRazaChange
                        )

                        AppDropdownField(
                            label = "Género",
                            selectedValue = state.genero,
                            options = generos,
                            onValueSelected = vm::onGeneroChange
                        )

                        AppTextField(
                            value = state.edad,
                            onValueChange = vm::onEdadChange,
                            label = "Edad",
                            singleLine = true
                        )

                        AppTextField(
                            value = state.peso,
                            onValueChange = vm::onPesoChange,
                            label = "Peso",
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text
                            )
                        )
                    }
                }

                item {
                    FormSection(
                        title = "Más detalles"
                    ) {
                        AppTextField(
                            value = state.ubicacion,
                            onValueChange = vm::onUbicacionChange,
                            label = "Ubicación",
                            singleLine = true
                        )

                        AppTextField(
                            value = state.estado,
                            onValueChange = vm::onEstadoChange,
                            label = "Estado",
                            singleLine = true
                        )

                        AppTextField(
                            value = state.descripcion,
                            onValueChange = vm::onDescripcionChange,
                            label = "Descripción",
                            minLines = 4
                        )
                    }
                }

                state.error?.let { errorMessage ->
                    item {
                        ErrorCard(
                            message = errorMessage
                        )
                    }
                }

                item {
                    SaveChangesActions(
                        isSaving = state.isSaving,
                        onSave = vm::saveChanges,
                        onCancel = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun EditPetHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = "Editar mascota",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Modifica la información de tu mascota",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditPhotosSection(
    currentPhotoUrls: List<String>,
    newPhotoUris: List<Uri>,
    onAddPhotos: () -> Unit,
    onRemoveExisting: (String) -> Unit,
    onRemoveNew: (Uri) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Fotos de tu mascota",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Puedes quitar fotos existentes o añadir nuevas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Button(
                onClick = onAddPhotos,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text("Añadir más fotos")
            }

            if (currentPhotoUrls.isNotEmpty()) {
                Text(
                    text = "Fotos actuales",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = currentPhotoUrls,
                        key = { photoUrl -> photoUrl }
                    ) { photoUrl ->
                        ExistingPhotoItem(
                            photoUrl = photoUrl,
                            isMain = currentPhotoUrls.firstOrNull() == photoUrl,
                            onRemove = {
                                onRemoveExisting(photoUrl)
                            }
                        )
                    }
                }
            }

            if (newPhotoUris.isNotEmpty()) {
                Text(
                    text = "Fotos nuevas",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = newPhotoUris,
                        key = { uri -> uri.toString() }
                    ) { uri ->
                        NewPhotoItem(
                            uri = uri,
                            onRemove = {
                                onRemoveNew(uri)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExistingPhotoItem(
    photoUrl: String,
    isMain: Boolean,
    onRemove: () -> Unit
) {
    Box {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Foto actual",
            modifier = Modifier
                .size(118.dp)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop
        )

        PhotoRemoveButton(
            onRemove = onRemove
        )

        if (isMain) {
            MainPhotoBadge()
        }
    }
}

@Composable
private fun NewPhotoItem(
    uri: Uri,
    onRemove: () -> Unit
) {
    Box {
        AsyncImage(
            model = uri,
            contentDescription = "Foto nueva",
            modifier = Modifier
                .size(118.dp)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop
        )

        PhotoRemoveButton(
            onRemove = onRemove
        )
    }
}

@Composable
private fun BoxScope.PhotoRemoveButton(
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .size(28.dp)
            .clickable(onClick = onRemove),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Eliminar foto",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(6.dp)
        )
    }
}

@Composable
private fun BoxScope.MainPhotoBadge() {
    Surface(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 4.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Foto principal",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(12.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Principal",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            content()
        }
    }
}

@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
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
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    enabled: Boolean = true,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = {
            if (enabled) {
                expanded = !expanded
            }
        }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = {
                Text(label)
            },
            placeholder = {
                Text(
                    text = if (enabled) {
                        "Selecciona una opción"
                    } else {
                        "Selecciona primero la especie"
                    }
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = enabled
                ),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = {
                expanded = false
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error
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
private fun SaveChangesActions(
    isSaving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !isSaving,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Guardar cambios",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving
        ) {
            Text(
                text = "Cancelar",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}