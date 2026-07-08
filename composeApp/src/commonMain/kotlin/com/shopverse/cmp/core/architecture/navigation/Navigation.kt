package com.shopverse.cmp.core.architecture.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.shopverse.cmp.screen.Dialog
import com.shopverse.cmp.screen.NavigationRoute

/**
 * Thin wrappers over navigation-compose's typed `composable`/`dialog` that (a) apply the shared
 * [NavigationRoute.typeMap] and (b) hand the destination its decoded route object via
 * [CustomNavEntry]. Mirrors ProvinCompose's navigation extensions.
 */
inline fun <reified T : NavigationRoute> NavGraphBuilder.composable(
    noinline content: @Composable AnimatedContentScope.(CustomNavEntry<T>) -> Unit,
) {
    composable<T>(typeMap = NavigationRoute.typeMap) { backStackEntry ->
        content(CustomNavEntry(backStackEntry, backStackEntry.toRoute<T>()))
    }
}

inline fun <reified T : Dialog> NavGraphBuilder.dialog(
    dialogProperties: DialogProperties = DialogProperties(),
    noinline content: @Composable (CustomNavEntry<T>) -> Unit,
) {
    dialog<T>(typeMap = NavigationRoute.typeMap, dialogProperties = dialogProperties) { backStackEntry ->
        content(CustomNavEntry(backStackEntry, backStackEntry.toRoute<T>()))
    }
}

data class CustomNavEntry<T : NavigationRoute>(
    val navBackStackEntry: NavBackStackEntry,
    val args: T,
)
