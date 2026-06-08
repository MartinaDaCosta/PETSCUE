package com.example.petscue.ui.profile.pet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pets
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage

private val BluePrimary = Color(0xFF1976D2)
private val BlueDark = Color(0xFF0D47A1)
private val BlueSoft = Color(0xFFEFF6FF)
private val BlueBorder = Color(0xFFB9D8FF)
private val BlueText = Color(0xFF215EAC)
private val BlueHint = Color(0xFF6B8DB8)

private val especies = listOf("Perro", "Gato")
private val generos = listOf("Macho", "Hembra")

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

    val razasDisponibles = when (state.especie) {
        "Perro" -> razasPerro
        "Gato" -> razasGato
        else -> emptyList()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FBFF)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 70.dp),
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
                            color = BlueHint
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
                            color = BlueHint
                        )

                        Button(
                            onClick = { photosLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BluePrimary,
                                contentColor = Color.White
                            )
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
                                items(state.photoUris) { uri ->
                                    PhotoItem(
                                        uri = uri,
                                        isMain = state.photoUris.firstOrNull() == uri,
                                        onRemove = {
                                            vm.removePhoto(uri)
                                        }
                                    )
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

                    BlueDropdownField(
                        label = "Especie",
                        selectedValue = state.especie,
                        options = especies,
                        onValueSelected = {
                            vm.onEspecieChange(it)
                            vm.onRazaChange("")
                        }
                    )

                    BlueDropdownField(
                        label = "Raza",
                        selectedValue = state.raza,
                        options = razasDisponibles,
                        enabled = state.especie.isNotBlank(),
                        onValueSelected = vm::onRazaChange
                    )

                    BlueDropdownField(
                        label = "Género",
                        selectedValue = state.genero,
                        options = generos,
                        onValueSelected = vm::onGeneroChange
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
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
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BluePrimary,
                            contentColor = Color.White,
                            disabledContainerColor = BlueBorder,
                            disabledContentColor = Color.White
                        )
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
private fun PhotoItem(
    uri: Uri,
    isMain: Boolean,
    onRemove: () -> Unit
) {
    Box {
        AsyncImage(
            model = uri,
            contentDescription = "Foto seleccionada",
            modifier = Modifier
                .size(118.dp)
                .clip(RoundedCornerShape(18.dp)),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.92f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Eliminar foto",
                tint = BlueDark,
                modifier = Modifier.size(16.dp)
            )
        }

        if (isMain) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(
                        color = BluePrimary,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Foto principal",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Principal",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlueDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    enabled: Boolean = true,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            placeholder = {
                Text(
                    text = if (enabled) "Selecciona una opción" else "Selecciona primero la especie"
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = if (enabled) BluePrimary else BlueHint
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BluePrimary,
                unfocusedBorderColor = BlueBorder,
                focusedLabelColor = BluePrimary,
                unfocusedLabelColor = Color(0xFF7A9BC4),
                cursorColor = BluePrimary,
                focusedTextColor = Color(0xFF173A63),
                unfocusedTextColor = Color(0xFF173A63),
                disabledTextColor = BlueHint,
                disabledBorderColor = BlueBorder,
                disabledLabelColor = BlueHint,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color(0xFFFDFEFF),
                disabledContainerColor = Color(0xFFF4F8FF)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            color = BlueDark
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