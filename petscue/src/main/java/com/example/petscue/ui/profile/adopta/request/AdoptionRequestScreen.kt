package com.example.petscue.ui.profile.adopta.request

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

private val BluePrimary = Color(0xFF1976D2)
private val BlueDark = Color(0xFF0D47A1)
private val BlueSoft = Color(0xFFEFF6FF)
private val BlueBorder = Color(0xFFB9D8FF)
private val BlueText = Color(0xFF215EAC)
private val BlueHint = Color(0xFF6B8DB8)

@Composable
fun AdoptionRequestScreen(
    onBack: () -> Unit,
    onRequestSent: (String) -> Unit,
    vm: AdoptionRequestViewModel = hiltViewModel()
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.submittedConversationId) {
        val conversationId = state.submittedConversationId
        if (!conversationId.isNullOrBlank()) {
            onRequestSent(conversationId)
            vm.consumeNavigation()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FBFF)
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }

            state.pet == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "No se ha encontrado el animal",
                        color = BlueDark,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                val pet = requireNotNull(state.pet)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 14.dp,
                        bottom = 70.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = BlueText
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = "Solicitud de adopción",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = BlueText,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Envía tu solicitud a la protectora",
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
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BlueBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = pet.fotos.firstOrNull(),
                                    contentDescription = pet.nombre,
                                    modifier = Modifier
                                        .size(92.dp)
                                        .background(BlueSoft, RoundedCornerShape(18.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = pet.nombre.ifBlank { "Animal" },
                                        style = MaterialTheme.typography.titleLarge,
                                        color = BlueDark,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = listOfNotNull(
                                            pet.especie.takeIf { it.isNotBlank() },
                                            pet.raza.takeIf { it.isNotBlank() },
                                            pet.edad.takeIf { it.isNotBlank() }
                                        ).joinToString(" · "),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BlueHint
                                    )
                                }
                            }
                        }
                    }

                    item {
                        FormSection(title = "Cuéntanos sobre ti") {
                            BlueTextField(
                                value = state.mensaje,
                                onValueChange = vm::onMensajeChange,
                                label = "Mensaje para la protectora",
                                minLines = 4,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = BluePrimary
                                    )
                                }
                            )

                            BlueTextField(
                                value = state.telefono,
                                onValueChange = vm::onTelefonoChange,
                                label = "Teléfono",
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = BluePrimary
                                    )
                                }
                            )

                            BlueTextField(
                                value = state.vivienda,
                                onValueChange = vm::onViviendaChange,
                                label = "Vivienda",
                                minLines = 2,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = BluePrimary
                                    )
                                }
                            )

                            BlueTextField(
                                value = state.experiencia,
                                onValueChange = vm::onExperienciaChange,
                                label = "Experiencia con animales",
                                minLines = 3,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = null,
                                        tint = BluePrimary
                                    )
                                }
                            )

                            BlueTextField(
                                value = state.otrosAnimales,
                                onValueChange = vm::onOtrosAnimalesChange,
                                label = "Otros animales en casa",
                                minLines = 2
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
                                onClick = vm::submitRequest,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                enabled = !state.isSubmitting,
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BluePrimary,
                                    contentColor = Color.White,
                                    disabledContainerColor = BlueBorder,
                                    disabledContentColor = Color.White
                                )
                            ) {
                                if (state.isSubmitting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Text("Enviar solicitud")
                                }
                            }

                            TextButton(
                                onClick = onBack,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isSubmitting
                            ) {
                                Text("Cancelar", color = BlueText)
                            }
                        }
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
    leadingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        leadingIcon = leadingIcon,
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