package com.example.petscue.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = PetscueBlue,
    onPrimary = PetscueLightOnPrimary,
    secondary = PetscueBlueLight,
    onSecondary = PetscueLightOnPrimary,
    background = PetscueLightBackground,
    onBackground = PetscueLightOnBackground,
    surface = PetscueLightSurface,
    onSurface = PetscueLightOnSurface,
    surfaceVariant = PetscueLightSurfaceVariant,
    onSurfaceVariant = PetscueLightOnBackground,
    error = PetscueError,
    onError = PetscueLightOnPrimary
)

private val DarkColors = darkColorScheme(
    primary = PetscueBlueLight,
    onPrimary = PetscueDarkOnPrimary,
    secondary = PetscueBlue,
    onSecondary = PetscueDarkOnPrimary,
    background = PetscueDarkBackground,
    onBackground = PetscueDarkOnBackground,
    surface = PetscueDarkSurface,
    onSurface = PetscueDarkOnSurface,
    surfaceVariant = PetscueDarkSurfaceVariant,
    onSurfaceVariant = PetscueDarkOnBackground,
    error = PetscueError,
    onError = PetscueDarkOnPrimary
)

@Composable
fun PetscueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.primary.toArgb()
            window.navigationBarColor = colors.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = PetscueTypography,
        shapes = PetscueShapes,
        content = content
    )
}