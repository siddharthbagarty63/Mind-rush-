package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = BgNearBlack,
    primaryContainer = NeonGreenGlow,
    onPrimaryContainer = NeonGreen,
    secondary = CyberCyan,
    onSecondary = BgNearBlack,
    secondaryContainer = CyberCyanGlow,
    onSecondaryContainer = CyberCyan,
    tertiary = ElectricPurple,
    onTertiary = TextPrimary,
    tertiaryContainer = ElectricPurpleGlow,
    onTertiaryContainer = ElectricPurple,
    background = BgNearBlack,
    onBackground = TextPrimary,
    surface = BgNavyDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceCardBorder,
    error = ErrorPink,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Enforce our custom cyberpunk neon palette for consistent arcade feel
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgNearBlack.toArgb()
            window.navigationBarColor = BgNearBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
