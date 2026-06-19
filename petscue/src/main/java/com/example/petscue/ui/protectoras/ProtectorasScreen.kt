package com.example.petscue.ui.protectoras

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.petscue.data.model.User
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectorasScreen(
    viewModel: ProtectorasViewModel = hiltViewModel(),
    onProtectoraClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val darkBlue = Color(0xFF0D47A1)
    val primaryBlue = Color(0xFF1565C0)
    val mediumBlue = Color(0xFF1E88E5)
    val softBlue = Color(0xFFE3F2FD)
    val paleBlue = Color(0xFFF1F7FF)

    var showFiltersSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleBlue)
            .padding(16.dp)
    ) {
        Text(
            text = "Protectoras",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = darkBlue
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = softBlue),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Buscar protectoras") },
                    placeholder = { Text("Nombre o username") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryBlue,
                        unfocusedBorderColor = mediumBlue,
                        focusedLabelColor = primaryBlue,
                        cursorColor = primaryBlue,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                if (state.query.isNotBlank() && state.suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        state.suggestions.forEach { suggestion ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.onSuggestionSelected(suggestion) },
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White
                            ) {
                                Text(
                                    text = suggestion,
                                    modifier = Modifier.padding(12.dp),
                                    color = darkBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showFiltersSheet = true },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filtros")
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = buildFilterSummary(state),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = darkBlue,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(state.filteredProtectoras, key = { it.uid }) { protectora ->
                ProtectoraCard(
                    protectora = protectora,
                    onClick = { onProtectoraClick(protectora.uid) }
                )
            }
        }
    }

    if (showFiltersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFiltersSheet = false },
            containerColor = softBlue
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = darkBlue
                )

                Spacer(modifier = Modifier.height(16.dp))

                NombreSortChips(
                    selected = state.nombreSort,
                    onSelected = viewModel::onNombreSortChanged
                )

                Spacer(modifier = Modifier.height(12.dp))

                DistanciaSortChips(
                    selected = state.distanciaSort,
                    onSelected = viewModel::onDistanciaSortChanged
                )

                Spacer(modifier = Modifier.height(12.dp))

                ComunidadDropdown(
                    comunidades = state.comunidadesDisponibles,
                    selectedComunidad = state.selectedComunidad,
                    onComunidadSelected = viewModel::onComunidadChanged
                )

                Spacer(modifier = Modifier.height(10.dp))

                ProvinciaDropdown(
                    provincias = state.provinciasDisponibles,
                    selectedProvincia = state.selectedProvincia,
                    onProvinciaSelected = viewModel::onProvinciaChanged
                )

                Spacer(modifier = Modifier.height(10.dp))

                MunicipioDropdown(
                    municipios = state.municipiosDisponibles,
                    selectedMunicipio = state.selectedMunicipio,
                    onMunicipioSelected = viewModel::onMunicipioChanged
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.clearFilters() },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = darkBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAltOff,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Borrar filtros")
                    }

                    TextButton(onClick = { showFiltersSheet = false }) {
                        Text("Cerrar", color = darkBlue)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun buildFilterSummary(state: ProtectorasUiState): String {
    val parts = mutableListOf<String>()

    parts += if (state.distanciaSort == DistanciaSort.CERCA_LEJOS) {
        "Más cerca primero"
    } else {
        "Más lejos primero"
    }

    parts += if (state.nombreSort == NombreSort.A_Z) "Nombre A-Z" else "Nombre Z-A"

    state.selectedComunidad?.let { parts += it }
    state.selectedProvincia?.let { parts += it }
    state.selectedMunicipio?.let { parts += "Municipio: $it" }

    return parts.joinToString(" · ")
}

@Composable
private fun NombreSortChips(
    selected: NombreSort,
    onSelected: (NombreSort) -> Unit
) {
    val primaryBlue = Color(0xFF1565C0)

    Column {
        Text(
            text = "Ordenar por nombre",
            style = MaterialTheme.typography.labelLarge,
            color = primaryBlue,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selected == NombreSort.A_Z,
                onClick = { onSelected(NombreSort.A_Z) },
                label = { Text("A-Z") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.SortByAlpha,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryBlue,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = selected == NombreSort.Z_A,
                onClick = { onSelected(NombreSort.Z_A) },
                label = { Text("Z-A") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.SortByAlpha,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun DistanciaSortChips(
    selected: DistanciaSort,
    onSelected: (DistanciaSort) -> Unit
) {
    val primaryBlue = Color(0xFF1565C0)

    Column {
        Text(
            text = "Ordenar por distancia",
            style = MaterialTheme.typography.labelLarge,
            color = primaryBlue,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selected == DistanciaSort.CERCA_LEJOS,
                onClick = { onSelected(DistanciaSort.CERCA_LEJOS) },
                label = { Text("Cerca-Lejos") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryBlue,
                    selectedLabelColor = Color.White
                )
            )

            FilterChip(
                selected = selected == DistanciaSort.LEJOS_CERCA,
                onClick = { onSelected(DistanciaSort.LEJOS_CERCA) },
                label = { Text("Lejos-Cerca") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryBlue,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComunidadDropdown(
    comunidades: List<String>,
    selectedComunidad: String?,
    onComunidadSelected: (String?) -> Unit
) {
    BlueDropdownField(
        label = "Comunidad",
        value = selectedComunidad ?: "Todas las comunidades",
        items = listOf("Todas las comunidades") + comunidades,
        onItemSelected = { selected ->
            onComunidadSelected(if (selected == "Todas las comunidades") null else selected)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProvinciaDropdown(
    provincias: List<String>,
    selectedProvincia: String?,
    onProvinciaSelected: (String?) -> Unit
) {
    BlueDropdownField(
        label = "Provincia",
        value = selectedProvincia ?: "Todas las provincias",
        items = listOf("Todas las provincias") + provincias,
        onItemSelected = { selected ->
            onProvinciaSelected(if (selected == "Todas las provincias") null else selected)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MunicipioDropdown(
    municipios: List<String>,
    selectedMunicipio: String?,
    onMunicipioSelected: (String?) -> Unit
) {
    BlueDropdownField(
        label = "Municipio",
        value = selectedMunicipio ?: "Todos los municipios",
        items = listOf("Todos los municipios") + municipios,
        onItemSelected = { selected ->
            onMunicipioSelected(if (selected == "Todos los municipios") null else selected)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BlueDropdownField(
    label: String,
    value: String,
    items: List<String>,
    onItemSelected: (String) -> Unit
) {
    val primaryBlue = Color(0xFF1565C0)
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryBlue,
                unfocusedBorderColor = primaryBlue,
                focusedLabelColor = primaryBlue,
                unfocusedLabelColor = primaryBlue,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color(0xFFE3F2FD)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProtectoraCard(
    protectora: User,
    onClick: () -> Unit
) {
    val darkBlue = Color(0xFF0D47A1)
    val mediumBlue = Color(0xFF1E88E5)
    val softBlue = Color(0xFFE3F2FD)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = softBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = protectora.photoUrl.ifBlank { null },
                contentDescription = "Foto de ${protectora.nombreProtectora}",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = protectora.nombreProtectora.ifBlank { "Protectora sin nombre" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = darkBlue
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "@${protectora.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = mediumBlue
                )

                Spacer(modifier = Modifier.height(6.dp))

                val ubicacion = listOf(
                    protectora.ciudad,
                    protectora.provincia,
                    protectora.comunidad
                ).filter { it.isNotBlank() }.joinToString(", ")

                if (ubicacion.isNotBlank()) {
                    Text(
                        text = ubicacion,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF355C8A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = darkBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${protectora.followers} seguidores",
                            style = MaterialTheme.typography.bodySmall,
                            color = darkBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}