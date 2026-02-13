package com.aigame.heartquest.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepRose = Color(0xFFB71C6E)
val WarmPink = Color(0xFFFF6B9D)
val SoftBlush = Color(0xFFFFC3D6)
val MidnightBlue = Color(0xFF1A1A3E)
val DeepPurple = Color(0xFF2D1B69)
val Twilight = Color(0xFF4A3880)
val GoldenGlow = Color(0xFFFFD700)
val SoftWhite = Color(0xFFFAF0F5)
val HeartRed = Color(0xFFFF1744)
val SuccessGreen = Color(0xFF4CAF50)

private val DarkColorScheme = darkColorScheme(
    primary = DeepRose,
    onPrimary = SoftWhite,
    primaryContainer = Twilight,
    onPrimaryContainer = SoftBlush,
    secondary = WarmPink,
    onSecondary = MidnightBlue,
    secondaryContainer = DeepPurple,
    onSecondaryContainer = SoftBlush,
    background = MidnightBlue,
    onBackground = SoftWhite,
    surface = Color(0xFF1E1E42),
    onSurface = SoftWhite,
    surfaceVariant = Color(0xFF2A2A55),
    onSurfaceVariant = Color(0xCCFFFFFF),
    error = HeartRed,
    onError = SoftWhite,
)

@Composable
fun HeartQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
