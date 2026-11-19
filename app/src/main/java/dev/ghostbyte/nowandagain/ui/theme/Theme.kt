package dev.ghostbyte.nowandagain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

val LightColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = Grey200,
    onPrimaryContainer = Black,
    secondary = Grey600,
    onSecondary = White,
    secondaryContainer = Grey200,       // progress track
    onSecondaryContainer = Black,
    tertiary = Grey600,
    onTertiary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = Grey150,
    onSurfaceVariant = Grey600,
    surfaceContainerLowest = White,
    surfaceContainerLow = Grey50,
    surfaceContainer = Grey100,         // task cards
    surfaceContainerHigh = Grey150,
    surfaceContainerHighest = Grey200,
    outline = Grey500,
    outlineVariant = Grey300,
    error = Red40,
    onError = White,
    errorContainer = RedContainerLight,
    onErrorContainer = Black,
    inverseSurface = Black,
    inverseOnSurface = White,
    inversePrimary = White,
)

val DarkColorScheme = darkColorScheme(
    primary = White,
    onPrimary = Black,
    primaryContainer = Grey750,
    onPrimaryContainer = White,
    secondary = Grey400,
    onSecondary = Black,
    secondaryContainer = Grey750,       // progress track
    onSecondaryContainer = White,
    tertiary = Grey400,
    onTertiary = Black,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = Grey800,
    onSurfaceVariant = Grey400,
    surfaceContainerLowest = Black,
    surfaceContainerLow = Grey900,
    surfaceContainer = Grey850,         // task cards
    surfaceContainerHigh = Grey800,
    surfaceContainerHighest = Grey750,
    outline = Grey500,
    outlineVariant = Grey750,
    error = Red80,
    onError = Black,
    errorContainer = RedContainerDark,
    onErrorContainer = White,
    inverseSurface = White,
    inverseOnSurface = Black,
    inversePrimary = Black,
)

@Composable
fun NowAndAgainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalStatusColors provides if (darkTheme) DarkStatus else LightStatus
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}