package com.example.petscue.ui.pet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage

private val BluePrimary = Color(0xFF4DA3FF)
private val BlueSoft = Color(0xFFEFF6FF)
private val BlueBorder = Color(0xFFB9D8FF)
private val BlueText = Color(0xFF215EAC)

@Composable
fun AddPetScreen(
    onBack: () -> Unit,
    onPetSaved: () -> Unit,
    vm: AddPetViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsState()

    val photosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        vm.onPhotosSelected(uris)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onPetSaved()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FBFF)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = BlueText
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Añadir mascota",
                            style = MaterialTheme.typography.headlineSmall,
                            color = BlueText,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Completa los datos y añade fotos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B8DB8)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BlueSoft),
                    border = BorderStroke(1.dp, BlueBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Fotos de tu mascota",
                            style = MaterialTheme.typography.titleMedium,
                            color = BlueText,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "La primera foto será la principal y se mostrará en el perfil.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B8DB8)
                        )

                        Button(
                            onClick = { photosLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Seleccionar fotos")
                        }

                        if (state.photoUris.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(state.photoUris) { index, uri ->
                                    Box {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Foto ${index + 1}",
                                            modifier = Modifier
                                                .size(118.dp)
                                                .clip(RoundedCornerShape(18.dp)),
                                            contentScale = ContentScale.Crop
                                        )

                                        if (index == 0) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(8.dp)
                                                    .background(
                                                        color = BluePrimary,
                                                        shape = CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Foto principal",
                                                    tint = Color.White,
                                                    modifier = Modifier
                                                        .padding(6.dp)
                                                        .size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                FormSection(title = "Información básica") {
                    BlueTextField(
                        value = state.nombre,
                        onValueChange = vm::onNombreChange,
                        label = "Nombre",
                        singleLine = true
                    )

                    BlueTextField(
                        value = state.especie,
                        onValueChange = vm::onEspecieChange,
                        label = "Especie",
                        singleLine = true
                    )

                    BlueTextField(
                        value = state.raza,
                        onValueChange = vm::onRazaChange,
                        label = "Raza",
                        singleLine = true
                    )

                    BlueTextField(
                        value = state.genero,
                        onValueChange = vm::onGeneroChange,
                        label = "Género",
                        singleLine = true
                    )

                    BlueTextField(
                        value = state.edad,
                        onValueChange = vm::onEdadChange,
                        label = "Edad",
                        singleLine = true
                    )

                    BlueTextField(
                        value = state.peso,
                        onValueChange = vm::onPesoChange,
                        label = "Peso",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                }
            }

            item {
                FormSection(title = "Más detalles") {
                    BlueTextField(
                        value = state.ubicacion,
                        onValueChange = vm::onUbicacionChange,
                        label = "Ubicación",
                        singleLine = true
                    )

                    BlueTextField(
                        value = state.estado,
                        onValueChange = vm::onEstadoChange,
                        label = "Estado",
                        singleLine = true
                    )

                    BlueTextField(
                        value = state.descripcion,
                        onValueChange = vm::onDescripcionChange,
                        label = "Descripción",
                        minLines = 4
                    )
                }
            }

            state.error?.let { errorMessage ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF1F1)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFFCACA)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = vm::savePet,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar mascota")
                        }
                    }

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading
                    ) {
                        Text("Cancelar", color = BlueText)
                    }
                }
            }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = BlueText,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun BlueTextField(
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
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = BlueBorder,
            focusedLabelColor = BluePrimary,
            unfocusedLabelColor = Color(0xFF7A9BC4),
            cursorColor = BluePrimary,
            focusedTextColor = Color(0xFF173A63),
            unfocusedTextColor = Color(0xFF173A63),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color(0xFFFDFEFF)
        )
    )
}