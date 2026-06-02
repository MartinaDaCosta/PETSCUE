package com.example.petscue.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class OnboardingPage(
    val emoji:       String,
    val title:       String,
    val description: String
)

private val pages = listOf(
    OnboardingPage("🔍", "BUSCA",
        "Anuncia la desaparición\nde tu mascota"),
    OnboardingPage("📢", "INFORMA",
        "Anuncia que has encontrado\nun animal perdido"),
    OnboardingPage("🏠", "ADOPTA",
        "Anuncia una adopción para\npoder encontrar una familia")
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF64B5F6), Color(0xFF1565C0))
                )
            )
    ) {
        // Contenido de la página
        AnimatedContent(targetState = currentPage, label = "onboarding") { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(pages[page].emoji, fontSize = 90.sp)
                Spacer(Modifier.height(32.dp))
                Text(
                    text          = pages[page].title,
                    color         = Color.White,
                    fontSize      = 36.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    letterSpacing = 3.sp
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text       = pages[page].description,
                    color      = Color.White.copy(alpha = 0.9f),
                    fontSize   = 18.sp,
                    textAlign  = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }

        // Dots indicadores
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            pages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentPage) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) Color.White
                            else Color.White.copy(alpha = 0.4f)
                        )
                )
            }
        }

        // Botones
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (currentPage < pages.lastIndex) currentPage++
                    else onFinished()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = Color(0xFF1565C0)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text       = if (currentPage < pages.lastIndex) "Siguiente" else "Empezar",
                    fontWeight = FontWeight.Bold
                )
            }

            if (currentPage < pages.lastIndex) {
                TextButton(
                    onClick  = onFinished,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Omitir", color = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}