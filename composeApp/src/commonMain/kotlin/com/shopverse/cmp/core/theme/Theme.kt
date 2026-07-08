package com.shopverse.cmp.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.shopverse.cmp.model.ThemeMode

private val Brand = Color(0xFF6C5CE7)
private val BrandDark = Color(0xFFB3A7FF)

/**
 * Full-bleed brand background for splash + onboarding. Pinned across light/dark to match the
 * Android app's `primaryMain` (#5047E5) so the two clients look identical on first launch.
 */
val ShopVerseIndigo = Color(0xFF5047E5)

private val LightColors = lightColorScheme(
    primary = Brand,
    secondary = Color(0xFF00B894),
)

private val DarkColors = darkColorScheme(
    primary = BrandDark,
    secondary = Color(0xFF55EFC4),
)

@Composable
fun ShopVerseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        content = content,
    )
}
