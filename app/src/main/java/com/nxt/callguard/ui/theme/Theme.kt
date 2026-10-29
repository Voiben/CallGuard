package com.nxt.callguard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GuardBlue = Color(0xFF1E88E5)
val GuardDanger = Color(0xFFE53935)
val GuardSafe = Color(0xFF43A047)

private val LightColors = lightColorScheme(
    primary = GuardBlue,
    error = GuardDanger,
    tertiary = GuardSafe
)

private val DarkColors = darkColorScheme(
    primary = GuardBlue,
    error = GuardDanger,
    tertiary = GuardSafe
)

@Composable
fun CallGuardTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
