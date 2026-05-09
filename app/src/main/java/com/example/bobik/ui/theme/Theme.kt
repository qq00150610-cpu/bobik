package com.example.bobik.ui.theme

import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0D0D1A)
val DarkSurface = Color(0xFF1A1A2E)
val DarkCard = Color(0xFF16213E)
val NeonCyan = Color(0xFF00F5FF)
val ElectricViolet = Color(0xFF7B2FFF)
val SoftLavender = Color(0xFFB388FF)
val WarmAmber = Color(0xFFFFAB40)
val SuccessGreen = Color(0xFF00E676)
val ErrorRed = Color(0xFFFF5252)

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BobikColorScheme = darkColorScheme(
    primary = ElectricViolet,
    secondary = NeonCyan,
    tertiary = SoftLavender,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun BobikTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BobikColorScheme,
        content = content
    )
}
