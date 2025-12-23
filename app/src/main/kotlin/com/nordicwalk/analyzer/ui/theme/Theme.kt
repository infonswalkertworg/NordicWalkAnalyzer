package com.nordicwalk.analyzer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF32B8C6),
    onPrimary = Color(0xFF003D45),
    primaryContainer = Color(0xFF1A6A75),
    onPrimaryContainer = Color(0xFF7FF5FF),
    secondary = Color(0xFF5E5260),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF7A7587),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFFC98A2E),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF1F2121),
    onBackground = Color(0xFFE5E5E5),
    surface = Color(0xFF262828),
    onSurface = Color(0xFFE5E5E5),
    error = Color(0xFFFF5459),
    onError = Color(0xFFFFFFFF)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2180A8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD4E3F0),
    onPrimaryContainer = Color(0xFF0D4E6F),
    secondary = Color(0xFF5E5260),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE7DDE7),
    onSecondaryContainer = Color(0xFF453F47),
    tertiary = Color(0xFFC98A2E),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFCFCFC),
    onBackground = Color(0xFF1F2121),
    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1F2121),
    error = Color(0xFFC01530),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun NordicWalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NordicWalkTypography,
        content = content
    )
}
