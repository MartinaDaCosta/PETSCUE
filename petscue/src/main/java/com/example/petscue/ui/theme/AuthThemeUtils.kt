package com.example.petscue.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun authBackgroundBrush(): Brush {
    val dark = MaterialTheme.colorScheme.background == PetscueDarkBackground
    return Brush.verticalGradient(
        colors = if (dark) {
            listOf(AuthGradientStartDark, AuthGradientEndDark)
        } else {
            listOf(AuthGradientStartLight, AuthGradientEndLight)
        }
    )
}

@Composable
fun AuthScreenContainer(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(authBackgroundBrush())
    ) {
        content()
    }
}

@Composable
fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.White.copy(alpha = 0.65f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color.White,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.White.copy(alpha = 0.85f),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedTrailingIconColor = Color.White,
    unfocusedTrailingIconColor = Color.White.copy(alpha = 0.85f),
    focusedLeadingIconColor = Color.White,
    unfocusedLeadingIconColor = Color.White.copy(alpha = 0.85f),
    focusedPlaceholderColor = Color.White.copy(alpha = 0.75f),
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.65f)
)

@Composable
fun authPrimaryButtonColors(): ButtonColors {
    return ButtonDefaults.buttonColors(
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.primary
    )
}

val AuthCardShape = RoundedCornerShape(14.dp)
val AuthTextFieldShape = RoundedCornerShape(20.dp)