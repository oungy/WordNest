package io.github.oungy.wordnest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF476550),
    onPrimary = Color.White,
    background = Color(0xFFF7F6F2),
    onBackground = Color(0xFF252A25),
    surface = Color(0xFFF7F6F2),
    onSurface = Color(0xFF252A25),
    onSurfaceVariant = Color(0xFF555D55),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB2CEB7),
    onPrimary = Color(0xFF1F3525),
    background = Color(0xFF171C18),
    onBackground = Color(0xFFE2E6DE),
    surface = Color(0xFF171C18),
    onSurface = Color(0xFFE2E6DE),
    onSurfaceVariant = Color(0xFFBAC3B9),
)

@Composable
fun WordNestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
