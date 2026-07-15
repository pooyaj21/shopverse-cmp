package com.shopverse.cmp.screen.main

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shopverse.cmp.core.architecture.navigation.composable
import com.shopverse.cmp.core.theme.NavUnselected
import com.shopverse.cmp.screen.Screen
import com.shopverse.cmp.screen.cart.CartRoute
import com.shopverse.cmp.screen.home.HomeRoute
import com.shopverse.cmp.screen.profile.ProfileRoute
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import shopversecmp.composeapp.generated.resources.Res
import shopversecmp.composeapp.generated.resources.ic_cart
import shopversecmp.composeapp.generated.resources.ic_home
import shopversecmp.composeapp.generated.resources.ic_profile

// Other screens ask the tab host to switch tabs through Main's SavedStateHandle — the CMP
// equivalent of Android's NavigatorScreenArgs.selectTabTag.
const val MAIN_SELECT_TAB_KEY = "main.selectTab"
const val MAIN_SELECT_TAB_CART = "cart"

private data class Tab(val screen: Screen, val icon: DrawableResource, val label: String)

private val tabs = listOf(
    Tab(Screen.Home, Res.drawable.ic_home, "Home"),
    Tab(Screen.Cart, Res.drawable.ic_cart, "Cart"),
    Tab(Screen.Profile, Res.drawable.ic_profile, "Profile"),
)

/** Tab host mirroring the Android navigator: Home / Cart / Profile with an icon-only bottom bar. */
@Composable
fun MainScreen(rootNavController: NavHostController, savedStateHandle: SavedStateHandle) {
    val tabNav = rememberNavController()

    val pendingTab by savedStateHandle
        .getStateFlow<String?>(MAIN_SELECT_TAB_KEY, null)
        .collectAsState()
    LaunchedEffect(pendingTab) {
        if (pendingTab == MAIN_SELECT_TAB_CART) {
            savedStateHandle[MAIN_SELECT_TAB_KEY] = null
            tabNav.navigate(Screen.Cart) {
                popUpTo(tabNav.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                val entry by tabNav.currentBackStackEntryAsState()
                val current = entry?.destination
                tabs.forEach { tab ->
                    val selected = current?.hierarchy?.any { it.hasRoute(tab.screen::class) } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            tabNav.navigate(tab.screen) {
                                popUpTo(tabNav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(painterResource(tab.icon), contentDescription = tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = NavUnselected,
                            indicatorColor = Color.Transparent,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = tabNav,
            startDestination = Screen.Home,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            composable<Screen.Home> {
                HomeRoute(navController = rootNavController, viewModel = koinViewModel())
            }
            composable<Screen.Cart> { CartRoute(navController = rootNavController) }
            composable<Screen.Profile> { ProfileRoute(navController = rootNavController) }
        }
    }
}
