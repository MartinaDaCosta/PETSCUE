package com.example.petscue.ui.protectoras

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.petscue.data.model.Protectora

// Colores del tema
private val AzulPrimario    = Color(0xFF1E88E5)
private val AzulSecundario  = Color(0xFF42A5F5)
private val AzulFondo       = Color(0xFFE3F2FD)
private val Blanco          = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectoraDetalleScreen(
    protectora: Protectora,
    onBack: () -> Unit
) {
    var busqueda        by remember { mutableStateOf("") }
    var categoriaActiva by remember { mutableStateOf("Todos") }

    val categorias = listOf("Todos", "Gatos", "Perros", "Conejos", "Aves", "Otros")

    // Animales ficticios — en producción vendrán de Firestore/Room
    val animales = remember { generarAnimalesFicticios(protectora.ciudad) }
    val animalesFiltrados = animales.filter { animal ->
        (categoriaActiva == "Todos" || animal.tipo == categoriaActiva) &&
                (busqueda.isEmpty() || animal.nombre.contains(busqueda, ignoreCase = true))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        // ── TOP BAR CON FLECHA ATRÁS ─────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(AzulPrimario, AzulSecundario))
                )
        ) {
            Column {
                // Flecha atrás
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Blanco)
                }

                // ── PERFIL ───────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Avatar con borde blanco + verificación
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Blanco),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = protectora.nombre.first().uppercase(),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = AzulPrimario
                            )
                        }
                        // Badge verificado
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color(0xFF1565C0), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null,
                                tint = Blanco, modifier = Modifier.size(11.dp))
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // Nombre + info
                    Column {
                        Text(
                            text = protectora.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Blanco,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))

                        // Descripción (bullet points)
                        val bullets = protectora.descripcion
                            .split(".")
                            .filter { it.isNotBlank() }
                            .take(3)
                        bullets.forEach { linea ->
                            Row {
                                Text("• ", color = Blanco.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall)
                                Text(linea.trim(),
                                    color = Blanco.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Dirección
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null,
                                tint = Blanco.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = "${protectora.ciudad}, ${protectora.provincia}",
                                color = Blanco.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                // ── STATS ─────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("12000", "Publicaciones")
                    StatDivider()
                    StatItem("80 mil", "Seguidores")
                    StatDivider()
                    StatItem("4000", "Seguidos")
                }

                // ── BOTONES ───────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* TODO: donar */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blanco),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Blanco, Blanco))
                        )
                    ) { Text("Donar", fontSize = 12.sp) }

                    OutlinedButton(
                        onClick = { /* TODO: seguir */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blanco),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Blanco, Blanco))
                        )
                    ) { Text("Seguir", fontSize = 12.sp) }

                    OutlinedButton(
                        onClick = { /* TODO: mensaje */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Blanco),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(listOf(Blanco, Blanco))
                        )
                    ) { Text("Enviar mensaje", fontSize = 11.sp) }
                }
            }
        }

        // ── BUSCADOR ─────────────────────────────────────────────────
        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("buscar") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        // ── SECCIÓN ANIMALES ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Animales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AzulPrimario)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Todos", fontSize = 13.sp, color = AzulPrimario)
                Text(" | ", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Filtros", fontSize = 13.sp, color = AzulPrimario)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null,
                    tint = AzulPrimario, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── CHIPS CATEGORÍA ───────────────────────────────────────────
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categorias.forEach { cat ->
                CategoriaChip(
                    label    = cat,
                    selected = cat == categoriaActiva,
                    onClick  = { categoriaActiva = cat }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── GRID DE ANIMALES ──────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            animalesFiltrados.chunked(2).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    fila.forEach { animal ->
                        AnimalCard(animal = animal, modifier = Modifier.weight(1f))
                    }
                    // Rellena si la fila tiene solo 1 elemento
                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(80.dp)) // espacio para bottom bar
    }
}

// ── COMPONENTES AUXILIARES ────────────────────────────────────────────────────

@Composable
private fun StatItem(valor: String, etiqueta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, fontWeight = FontWeight.Bold, color = Blanco,
            style = MaterialTheme.typography.bodyMedium)
        Text(etiqueta, color = Blanco.copy(alpha = 0.8f),
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .height(28.dp)
            .width(1.dp)
            .background(Blanco.copy(alpha = 0.4f))
    )
}

@Composable
private fun CategoriaChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = androidx.compose.ui.Modifier
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (selected) AzulPrimario else AzulFondo)
                .then(Modifier.padding(0.dp))
                .let { mod ->
                    mod.then(
                        Modifier.clickable(onClick = onClick)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val emoji = when (label) {
                "Gatos"  -> "🐱"
                "Perros" -> "🐶"
                "Conejos"-> "🐰"
                "Aves"   -> "🐦"
                "Todos"  -> "🐾"
                else     -> "🐾"
            }
            Text(emoji, fontSize = 22.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) AzulPrimario else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun AnimalCard(animal: AnimalItem, modifier: Modifier = Modifier) {
    var favorito by remember { mutableStateOf(false) }

    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box {
                // Foto del animal
                AsyncImage(
                    model             = animal.imageUrl,
                    contentDescription = animal.nombre,
                    contentScale      = ContentScale.Crop,
                    modifier          = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                )
                // Botón favorito
                IconButton(
                    onClick   = { favorito = !favorito },
                    modifier  = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (favorito) Icons.Filled.Favorite
                        else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (favorito) Color.Red else AzulPrimario,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(animal.nombre,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium)
                Text("${animal.sexo}, ${animal.edad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null,
                        tint = AzulPrimario, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(animal.ubicacion,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

// ── DATA CLASS + DATOS FICTICIOS ──────────────────────────────────────────────

data class AnimalItem(
    val nombre:   String,
    val tipo:     String,
    val sexo:     String,
    val edad:     String,
    val ubicacion:String,
    val imageUrl: String
)

private fun generarAnimalesFicticios(ciudad: String): List<AnimalItem> = listOf(
    AnimalItem("Olivia", "Gatos",  "Hembra", "2 años",  ciudad, "https://placekitten.com/300/300"),
    AnimalItem("Pedro",  "Perros", "Macho",  "3 años",  ciudad, "https://placedog.net/300/300"),
    AnimalItem("Luna",   "Gatos",  "Hembra", "1 año",   ciudad, "https://placekitten.com/301/301"),
    AnimalItem("Max",    "Perros", "Macho",  "4 años",  ciudad, "https://placedog.net/301/301"),
    AnimalItem("Bambi",  "Conejos","Hembra", "6 meses", ciudad, "https://picsum.photos/seed/rabbit1/300/300"),
    AnimalItem("Kira",   "Gatos",  "Hembra", "3 años",  ciudad, "https://placekitten.com/302/302"),
    AnimalItem("Rex",    "Perros", "Macho",  "2 años",  ciudad, "https://placedog.net/302/302"),
    AnimalItem("Coco",   "Aves",   "Macho",  "1 año",   ciudad, "https://picsum.photos/seed/bird1/300/300"),
)