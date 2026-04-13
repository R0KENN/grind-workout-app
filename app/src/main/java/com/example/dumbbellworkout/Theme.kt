package com.example.dumbbellworkout

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple = Color(0xFF6C63FF)
val Red = Color(0xFFFF6B6B)
val Green = Color(0xFF4CAF50)
val Orange = Color(0xFFFF9800)
val DarkBg = Color(0xFF000000)
val DarkSurface = Color(0xFF0A0A0F)
val DarkSurfaceVariant = Color(0xFF12121A)
val TextSecondary = Color(0xFFB0B0C0)

private val DarkColors = darkColorScheme(
    primary = Purple,
    secondary = Red,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = TextSecondary
)

@Composable
fun WorkoutTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}
