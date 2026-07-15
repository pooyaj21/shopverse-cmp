package com.shopverse.cmp.core.deeplink

import androidx.navigation.NavHostController
import com.shopverse.cmp.screen.Screen
import com.shopverse.cmp.screen.main.MAIN_SELECT_TAB_CART
import com.shopverse.cmp.screen.main.MAIN_SELECT_TAB_HOME
import com.shopverse.cmp.screen.main.MAIN_SELECT_TAB_KEY
import com.shopverse.cmp.screen.main.MAIN_SELECT_TAB_PROFILE

/**
 * Port of the Android app's DeepLinkLauncher: platform entry points (MainActivity intents,
 * SwiftUI's onOpenURL) [enqueue] URIs, which wait in a FIFO queue until the launcher is
 * [attach]ed to a NavController AND the tab host is on the back stack — the compose stand-in
 * for Android's "NavHostFragment set + AppStage.ESTABLISHED + app fully opened" gates, so
 * splash/onboarding always finish their own routing first.
 *
 * Adaptations from Android:
 *  - A Kotlin `object` with explicit [attach]/[detach] from composition replaces the
 *    Koin-injected class with WeakReferences (the queue must accept URIs before Koin starts).
 *  - Tab links select tabs through Main's SavedStateHandle instead of grabbing the tab bar view.
 *  - `product/<slug>` carries the slug, not the id — the CMP product route is slug-based.
 *  - OrderDetail is NOT login-gated (Android's launcher gates it): the order-detail endpoint is
 *    public by UUID and docs require the QR/deeplink to work logged-out.
 *
 * Supported links (scheme `shopverse://`):
 *  home | cart | profile | product/<slug> | account | order | order/<id> | orders | orders/<id>
 *  (`orders` accepted alongside Android's `order` — it's what the backend's deeplinks use.)
 */
object DeepLinkLauncher {

    private val queue = ArrayDeque<String>()
    private var navController: NavHostController? = null
    private var isLoggedIn: (() -> Boolean)? = null
    private var openLoginListener: ((pendingUri: String) -> Unit)? = null

    fun attach(
        navController: NavHostController,
        isLoggedIn: () -> Boolean,
        openLoginListener: (pendingUri: String) -> Unit,
    ) {
        this.navController = navController
        this.isLoggedIn = isLoggedIn
        this.openLoginListener = openLoginListener
        dequeueIfPossible()
    }

    fun detach() {
        navController = null
        isLoggedIn = null
        openLoginListener = null
    }

    fun enqueue(uri: String) {
        queue.addLast(uri)
        dequeueIfPossible()
    }

    /** Called when the back stack changes — the "app became established" trigger. */
    fun onNavStateChanged() {
        dequeueIfPossible()
    }

    private fun dequeueIfPossible() {
        val nc = navController ?: return
        // Established = past splash/onboarding: the tab host is on the back stack.
        runCatching { nc.getBackStackEntry(Screen.Main) }.getOrNull() ?: return
        while (queue.isNotEmpty()) {
            open(nc, queue.removeFirst())
        }
    }

    private fun open(nc: NavHostController, uri: String) {
        val action = uri.toAction() ?: return
        if (action.isLoginNeeded && isLoggedIn?.invoke() != true) {
            // The listener shows the auth sheet and re-enqueues the URI on success (Android's
            // DialogConfig.Login flow).
            openLoginListener?.invoke(uri)
            return
        }
        when (action) {
            Action.Home -> selectTab(nc, MAIN_SELECT_TAB_HOME)
            Action.Cart -> selectTab(nc, MAIN_SELECT_TAB_CART)
            Action.Profile -> selectTab(nc, MAIN_SELECT_TAB_PROFILE)
            is Action.ProductDetail -> nc.navigate(Screen.Product(action.slug))
            Action.Account -> nc.navigate(Screen.Account)
            Action.Orders -> nc.navigate(Screen.Orders)
            is Action.OrderDetail -> nc.navigate(Screen.OrderDetail(action.id))
        }
    }

    private fun selectTab(nc: NavHostController, tab: String) {
        val mainEntry = runCatching { nc.getBackStackEntry(Screen.Main) }.getOrNull() ?: return
        mainEntry.savedStateHandle[MAIN_SELECT_TAB_KEY] = tab
        nc.popBackStack(route = Screen.Main, inclusive = false)
    }

    private sealed class Action(val isLoginNeeded: Boolean = false) {
        data object Home : Action()
        data object Cart : Action()
        data object Profile : Action()
        data class ProductDetail(val slug: String) : Action()
        data object Account : Action(true)
        data object Orders : Action(true)
        data class OrderDetail(val id: String) : Action()
    }

    private fun String.toAction(): Action? {
        val scheme = substringBefore("://", missingDelimiterValue = "")
        if (!scheme.equals("shopverse", ignoreCase = true)) return null
        val segments = substringAfter("://")
            .substringBefore('?')
            .substringBefore('#')
            .split('/')
            .filter { it.isNotBlank() }
        val host = segments.firstOrNull()?.lowercase() ?: return null
        val firstSegment = segments.getOrNull(1)
        return when (host) {
            "home" -> Action.Home
            "cart" -> Action.Cart
            "profile" -> Action.Profile
            "product" -> firstSegment?.let { Action.ProductDetail(it) }
            "account" -> Action.Account
            "order", "orders" ->
                if (firstSegment == null) Action.Orders else Action.OrderDetail(firstSegment)
            else -> null
        }
    }
}
