package io.github.oungy.wordnest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal val ControlShape = RoundedCornerShape(17.dp)
internal val PanelShape = RoundedCornerShape(25.dp)
internal val SheetShape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)

private val LightColors = lightColorScheme(
    primary = Color(0xFF476550),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFEBDD),
    onPrimaryContainer = Color(0xFF274330),
    background = Color(0xFFF7F6F2),
    onBackground = Color(0xFF252A25),
    surface = Color(0xFFF7F6F2),
    onSurface = Color(0xFF252A25),
    onSurfaceVariant = Color(0xFF555D55),
    surfaceVariant = Color(0xFFE5E8E0),
    surfaceContainer = Color(0xFFEFEEE8),
    surfaceContainerHigh = Color(0xFFFCFBF7),
    outline = Color(0xFF737B70),
    outlineVariant = Color(0xFFCFD4CA),
    secondary = Color(0xFF576357),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1E9DD),
    onSecondaryContainer = Color(0xFF354437),
    error = Color(0xFF9C3833),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB2CEB7),
    onPrimary = Color(0xFF1F3525),
    primaryContainer = Color(0xFF334B3A),
    onPrimaryContainer = Color(0xFFDBEDDD),
    background = Color(0xFF171C18),
    onBackground = Color(0xFFE2E6DE),
    surface = Color(0xFF171C18),
    onSurface = Color(0xFFE2E6DE),
    onSurfaceVariant = Color(0xFFBAC3B9),
    surfaceVariant = Color(0xFF36413A),
    surfaceContainer = Color(0xFF232C26),
    surfaceContainerHigh = Color(0xFF28332B),
    outline = Color(0xFF929D92),
    outlineVariant = Color(0xFF46534A),
    secondary = Color(0xFFB8C7B6),
    onSecondary = Color(0xFF263529),
    secondaryContainer = Color(0xFF39483B),
    onSecondaryContainer = Color(0xFFD8E6D7),
    error = Color(0xFFFFB4AB),
)

@Composable
fun WordNestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        shapes = Shapes(small = ControlShape, medium = ControlShape, large = PanelShape),
        content = content,
    )
}
