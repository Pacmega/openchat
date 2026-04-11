package com.openrouter.chat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2481CC),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCF8C6),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFFEEFFEE),
    onSecondary = Color(0xFF1A1A1A),
    background = Color.White,
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFE0E0E0),
    error = Color(0xFFB00020),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2481CC),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1A3E52),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF2A2A2A),
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF3A3A3A),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun OpenRouterChatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography(),
        content = content
    )
}

val UserBubble = Color(0xFFDCF8C6)
val ModelBubble = Color(0xFFE5E5EA)
val Timestamp = Color(0xFF8E8E93)