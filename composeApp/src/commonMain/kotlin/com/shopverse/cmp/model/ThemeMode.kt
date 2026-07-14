package com.shopverse.cmp.model

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
        ThemeMode.SYSTEM -> "System"
    }
