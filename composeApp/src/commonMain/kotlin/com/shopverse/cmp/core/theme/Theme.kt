package com.shopverse.cmp.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.shopverse.cmp.model.ThemeMode

// Brand palette, matched 1:1 to the Android app's AppColorProvider.
val ShopVerseIndigo = Color(0xFF5047E5) // primaryMain + splash/onboarding background
val AddToCartBlue = Color(0xFF4F46E5)
val DiscountRed = Color(0xFFE53935)
val RatingStar = Color(0xFFF5A623)
val PriceMuted = Color(0xFF9AA0A6)
val NavUnselected = Color(0xFF808080)

private val LightColors = lightColorScheme(
    primary = ShopVerseIndigo,
    onPrimary = Color.White,
    secondary = Color(0xFF00B894),
    background = Color(0xFFFDFAE8),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFEFEDE3),
    onSurfaceVariant = Color(0xFF000000),
)

private val DarkColors = darkColorScheme(
    primary = ShopVerseIndigo,
    onPrimary = Color.White,
    secondary = Color(0xFF55EFC4),
    background = Color(0xFF020517),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1F1F1F),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFFFFFFF),
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
