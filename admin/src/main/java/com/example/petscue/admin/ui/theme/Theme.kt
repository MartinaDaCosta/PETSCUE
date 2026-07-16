package com.example.petscue.admin.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimaryDark,
    onPrimary = BlueOnPrimaryDark,
    primaryContainer = BluePrimaryContainerDark,
    onPrimaryContainer = BlueOnPrimaryContainerDark,

    secondary = BlueSecondaryDark,
    onSecondary = BlueOnSecondaryDark,
    secondaryContainer = BlueSecondaryContainerDark,
    onSecondaryContainer = BlueOnSecondaryContainerDark,

    background = BlueBackgroundDark,
    onBackground = BlueOnBackgroundDark,
    surface = BlueSurfaceDark,
    onSurface = BlueOnSurfaceDark,
    surfaceVariant = BlueSurfaceVariantDark,
    onSurfaceVariant = BlueOnSurfaceVariantDark,

    error = BlueError,
    onError = BlueOnError
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = BlueOnPrimary,
    primaryContainer = BluePrimaryContainer,
    onPrimaryContainer = BlueOnPrimaryContainer,

    secondary = BlueSecondary,
    onSecondary = BlueOnSecondary,
    secondaryContainer = BlueSecondaryContainer,
    onSecondaryContainer = BlueOnSecondaryContainer,

    tertiary = BlueTertiary,
    onTertiary = BlueOnTertiary,

    background = BlueBackground,
    onBackground = BlueOnBackground,
    surface = BlueSurface,
    onSurface = BlueOnSurface,
    surfaceVariant = BlueSurfaceVariant,
    onSurfaceVariant = BlueOnSurfaceVariant,

    error = BlueError,
    onError = BlueOnError
)

@Composable
fun PETSCUETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}