package com.jinelle.bookshelf.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * This is the app's overall UI theme (library screen, toolbars, dialogs) -- it does NOT
 * affect the in-book reading colors, which stay independently customizable per-book via
 * ReaderSettingsSheet (a light "White" theme app shell shouldn't force a light page
 * color while reading, and vice versa). See THEMING.md at the project root for the
 * full color mapping and how to adjust it.
 */
private val LightColors = lightColorScheme(
    primary = SlateBlue,
    onPrimary = WarmCream,
    primaryContainer = SlateBlueContainer,
    onPrimaryContainer = CharcoalText,
    secondary = SageMist,
    onSecondary = CharcoalText,
    secondaryContainer = SageMist,
    onSecondaryContainer = CharcoalText,
    background = WarmCream,
    onBackground = CharcoalText,
    surface = WarmCream,
    onSurface = CharcoalText,
    surfaceVariant = SageMist,
    onSurfaceVariant = CharcoalText,
    outline = CharcoalText.copy(alpha = 0.4f)
)

private val DarkColors = darkColorScheme(
    primary = WarmParchment,
    onPrimary = BlackCherryBark,
    primaryContainer = WarmParchmentDim,
    onPrimaryContainer = BlackCherryBark,
    secondary = SmokedMauve,
    onSecondary = WarmParchment,
    secondaryContainer = SmokedMauveContainer,
    onSecondaryContainer = WarmParchment,
    background = BlackCherryBark,
    onBackground = WarmParchment,
    surface = SmokedMauve,
    onSurface = WarmParchment,
    surfaceVariant = SmokedMauveContainer,
    onSurfaceVariant = WarmParchment,
    outline = WarmParchment.copy(alpha = 0.4f)
)

@Composable
fun BookshelfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
