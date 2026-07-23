package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BoldLightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PurpleOnPrimary,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = PurpleOnContainer,
    secondary = OnSecondaryPill,
    onSecondary = Color.White,
    secondaryContainer = SecondaryPill,
    onSecondaryContainer = OnSecondaryPill,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = NavigationBg,
    onSurfaceVariant = Color(0xFF49454F),
    outline = M3Outline,
    outlineVariant = M3OutlineVariant,
    inverseSurface = DarkNavyContainer,
    inverseOnSurface = DarkNavyText,
    inversePrimary = DarkNavySubtext
)

private val BoldDarkColorScheme = darkColorScheme(
    primary = DarkNavyText,
    onPrimary = DarkNavyContainer,
    primaryContainer = DarkNavyContainer,
    onPrimaryContainer = DarkNavySubtext,
    secondary = DarkNavySubtext,
    onSecondary = DarkNavyContainer,
    secondaryContainer = Color(0xFF3B236E),
    onSecondaryContainer = DarkNavySubtext,
    background = Color(0xFF140C24),
    onBackground = Color(0xFFEAE6F2),
    surface = Color(0xFF1D1430),
    onSurface = Color(0xFFEAE6F2),
    surfaceVariant = Color(0xFF281C42),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = M3OutlineVariant,
    outlineVariant = M3Outline,
    inverseSurface = LightBackground,
    inverseOnSurface = LightOnBackground,
    inversePrimary = PurplePrimary
)

@Composable
fun PromptGeneratorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BoldDarkColorScheme else BoldLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
