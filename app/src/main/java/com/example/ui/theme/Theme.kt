package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Amber500,
    onPrimary = Zinc950,
    primaryContainer = Amber900,
    onPrimaryContainer = Amber300,
    secondary = Amber400,
    onSecondary = Zinc950,
    secondaryContainer = Zinc800,
    onSecondaryContainer = Amber300,
    tertiary = Amber300,
    onTertiary = Zinc950,
    background = Zinc950,
    onBackground = Zinc50,
    surface = Zinc900,
    onSurface = Zinc50,
    surfaceVariant = Zinc850,
    onSurfaceVariant = Zinc300,
    outline = Zinc700,
    outlineVariant = Zinc800
)

@Composable
fun CartaTheme(
    darkTheme: Boolean = true, // By default dark luxury theme as requested
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
