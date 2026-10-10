package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LudoDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE53935),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB71C1C),
    onPrimaryContainer = Color(0xFFFFCDD2),
    secondary = Color(0xFF1E88E5),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0D47A1),
    onSecondaryContainer = Color(0xFFBBDEFB),
    tertiary = Color(0xFFFFB300),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFFFF6F00),
    onTertiaryContainer = Color(0xFFFFECB3),
    background = Color(0xFF0F111A),
    onBackground = Color.White,
    surface = Color(0xFF1A1D2E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2E3458),
    onSurfaceVariant = Color(0xFFECEFF1)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LudoDarkColorScheme,
        typography = Typography,
        content = content
    )
}
