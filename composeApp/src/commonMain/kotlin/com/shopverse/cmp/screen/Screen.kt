package com.shopverse.cmp.screen

import androidx.navigation.NavType
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KType

/**
 * Base for every navigation destination. [typeMap] registers custom NavTypes for complex,
 * `@Serializable` route arguments — empty for now since all ShopVerse routes use primitive args.
 */
abstract class NavigationRoute {
    companion object {
        val typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> get() = emptyMap()
    }
}

/** Full-screen destinations. Mirrors the Android app's screen set. */
@Serializable
sealed class Screen : NavigationRoute() {

    @Serializable
    data object Splash : Screen()

    @Serializable
    data object Onboarding : Screen()

    // Bottom-nav host wrapping the Home / Cart / Profile tabs.
    @Serializable
    data object Main : Screen()

    @Serializable
    data object Home : Screen()

    @Serializable
    data class Product(val slug: String) : Screen()

    @Serializable
    data object Cart : Screen()

    @Serializable
    data object Orders : Screen()

    // Public by design: `shopverse://orders/<id>` opens this even when logged out.
    @Serializable
    data class OrderDetail(val orderId: String) : Screen()

    @Serializable
    data object Profile : Screen()

    // Pushed from Profile's "Profile" row — shows the saved account + delete account.
    @Serializable
    data object Account : Screen()
}

/**
 * Dialog destinations. Currently empty: auth ships as an in-screen ModalBottomSheet
 * (screen/auth/AuthBottomSheet) like Android's AuthBottomSheetFragment, not a nav dialog.
 */
@Serializable
sealed class Dialog : NavigationRoute()
