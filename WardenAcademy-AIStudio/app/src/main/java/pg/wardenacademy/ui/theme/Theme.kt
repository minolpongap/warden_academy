package pg.wardenacademy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// PNG flag-inspired palette: red, black and gold.
val PngRed = Color(0xFFB71C1C)
val PngRedDark = Color(0xFF8E0000)
val PngGold = Color(0xFFF9A825)
val PngBlack = Color(0xFF1A1A1A)
val Success = Color(0xFF2E7D32)
val Danger = Color(0xFFC62828)

private val LightColors = lightColorScheme(
    primary = PngRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD5),
    onPrimaryContainer = Color(0xFF410001),
    secondary = PngGold,
    onSecondary = PngBlack,
    secondaryContainer = Color(0xFFFFF0C2),
    onSecondaryContainer = Color(0xFF3A2C00),
    tertiary = PngBlack,
    background = Color(0xFFFFFBFA),
    surface = Color(0xFFFFFBFA),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB4A9),
    onPrimary = Color(0xFF680003),
    primaryContainer = PngRedDark,
    onPrimaryContainer = Color(0xFFFFDAD5),
    secondary = PngGold,
    onSecondary = PngBlack,
    secondaryContainer = Color(0xFF574400),
    onSecondaryContainer = Color(0xFFFFE08A),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
)

@Composable
fun WardenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
