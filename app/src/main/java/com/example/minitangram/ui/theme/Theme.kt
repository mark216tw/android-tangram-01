package com.example.minitangram.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class DisplayMode { SYSTEM, LIGHT, DARK }

private val PaperLight = lightColorScheme(
    primary = Color(0xFF9E3528),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3D3C8),
    onPrimaryContainer = Color(0xFF39110D),
    secondary = Color(0xFF42675A),
    tertiaryContainer = Color(0xFFFFE9B8),
    onTertiaryContainer = Color(0xFF3E2E00),
    background = Color(0xFFF5EDDD),
    surface = Color(0xFFFFF9ED),
    surfaceVariant = Color(0xFFE7DAC4),
    onBackground = Color(0xFF29241E),
    onSurface = Color(0xFF29241E),
    outline = Color(0xFF827564)
)

private val InkDark = darkColorScheme(
    primary = Color(0xFFE99A87),
    onPrimary = Color(0xFF5D160E),
    primaryContainer = Color(0xFF7E261B),
    onPrimaryContainer = Color(0xFFFFDAD1),
    secondary = Color(0xFF9BCBB9),
    tertiaryContainer = Color(0xFF5A4618),
    onTertiaryContainer = Color(0xFFFFE4A3),
    background = Color(0xFF1D1A17),
    surface = Color(0xFF28231E),
    surfaceVariant = Color(0xFF494139),
    onBackground = Color(0xFFECE1D3),
    onSurface = Color(0xFFECE1D3),
    outline = Color(0xFF9E9182)
)

@Composable
fun MiniTangramTheme(mode: DisplayMode, content: @Composable () -> Unit) {
    val dark = when (mode) {
        DisplayMode.SYSTEM -> isSystemInDarkTheme()
        DisplayMode.LIGHT -> false
        DisplayMode.DARK -> true
    }
    val colors = if (dark) InkDark else PaperLight
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            window.navigationBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MaterialTheme(colorScheme = colors, content = content)
}
