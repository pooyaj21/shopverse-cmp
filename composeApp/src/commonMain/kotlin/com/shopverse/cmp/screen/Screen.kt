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
}

/** Dialog / bottom-sheet destinations. */
@Serializable
sealed class Dialog : NavigationRoute() {

    @Serializable
    data object Login : Dialog()

    @Serializable
    data object SignUp : Dialog()
}
