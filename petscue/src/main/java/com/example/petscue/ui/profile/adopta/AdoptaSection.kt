package com.example.petscue.ui.profile.adopta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.petscue.data.model.Pet

private val BluePrimary = Color(0xFF1976D2)
private val BlueDark = Color(0xFF0D47A1)
private val BlueSoft = Color(0xFFEFF6FF)
private val BlueBorder = Color(0xFFB9D8FF)

@Composable
private fun AdoptaSection(
    pets: List<Pet>,
    onAddPet: () -> Unit,
    onPetClick: (String) -> Unit
) {
    val rows = (pets.size + 1) / 2
    val gridHeight = if (pets.isEmpty()) 0.dp else (rows * 320).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Adopta",
                style = MaterialTheme.typography.titleLarge,
                color = BlueDark,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onAddPet) {
                Text(
                    text = "Añadir",
                    color = BluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (pets.isEmpty()) {
            Text("No hay animales en adopción ahora mismo.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pets) { pet ->
                    AdoptPetCard(
                        pet = pet,
                        onClick = { onPetClick(pet.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdoptPetCard(
    pet: Pet,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BlueBorder)
    ) {
        Column {
            val petImage = pet.fotos.firstOrNull()

            if (!petImage.isNullOrBlank()) {
                AsyncImage(
                    model = petImage,
                    contentDescription = "Foto de ${pet.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(BlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = pet.nombre.ifBlank { "Sin nombre" },
                    style = MaterialTheme.typography.titleMedium,
                    color = BlueDark,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                AdoptPetInfoLine("Edad", pet.edad)
                AdoptPetInfoLine("Género", pet.genero)
                AdoptPetInfoLine("Especie", pet.especie)
                AdoptPetInfoLine("Raza", pet.raza)
            }
        }
    }
}

@Composable
private fun AdoptPetInfoLine(
    label: String,
    value: String
) {
    Text(
        text = "$label: ${value.ifBlank { "-" }}",
        style = MaterialTheme.typography.bodySmall,
        color = BluePrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}