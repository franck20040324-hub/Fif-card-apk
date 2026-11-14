package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AmoledDarkColorScheme = darkColorScheme(
    primary = FifOrange,
    onPrimary = Color.White,
    primaryContainer = FifOrangeContainerDark,
    onPrimaryContainer = FifOrangeSoft,
    secondary = FifOrangeSoft,
    onSecondary = Color.Black,
    background = AmoledBlack,
    onBackground = AmoledTextPrimary,
    surface = AmoledSurface,
    onSurface = AmoledTextPrimary,
    surfaceVariant = AmoledSurfaceVariant,
    onSurfaceVariant = AmoledTextSecondary,
    outline = AmoledBorder,
    outlineVariant = Color(0xFF1C202A),
    error = LineAttRed,
    onError = Color.White
)

private val BrightWhiteColorScheme = lightColorScheme(
    primary = FifOrange,
    onPrimary = Color.White,
    primaryContainer = FifOrangeContainerLight,
    onPrimaryContainer = Color(0xFF5A2400),
    secondary = Color(0xFF1E293B),
    onSecondary = Color.White,
    background = BrightWhite,
    onBackground = BrightTextPrimary,
    surface = BrightSurface,
    onSurface = BrightTextPrimary,
    surfaceVariant = BrightSurfaceVariant,
    onSurfaceVariant = BrightTextSecondary,
    outline = BrightBorder,
    outlineVariant = Color(0xFFE2E8F0),
    error = LineAttRed,
    onError = Color.White
)

@Composable
fun FifCardTheme(
    darkTheme: Boolean,
    isArabic: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AmoledDarkColorScheme else BrightWhiteColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = fifTypography(isArabic),
        content = content
    )
}
