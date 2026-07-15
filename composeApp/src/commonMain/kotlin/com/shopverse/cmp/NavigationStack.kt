package com.shopverse.cmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.shopverse.cmp.core.architecture.navigation.composable
import com.shopverse.cmp.core.deeplink.DeepLinkLauncher
import com.shopverse.cmp.network.useCase.IsLoggedInUseCase
import com.shopverse.cmp.screen.Screen
import com.shopverse.cmp.screen.account.AccountRoute
import com.shopverse.cmp.screen.auth.AuthBottomSheet
import com.shopverse.cmp.screen.main.MainScreen
import com.shopverse.cmp.screen.onboarding.OnboardingRoute
import com.shopverse.cmp.screen.orderDetail.OrderDetailRoute
import com.shopverse.cmp.screen.orders.OrdersRoute
import com.shopverse.cmp.screen.product.ProductRoute
import com.shopverse.cmp.screen.splash.SplashRoute
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavigationStack(navController: NavHostController) {
    // Wire the DeepLinkLauncher: URIs enqueued by the platforms dequeue once the tab host is
    // on the back stack. Login-gated links (account, orders) open the auth sheet and re-enqueue
    // on success — the same flow as Android's DialogConfig.Login.
    val isLoggedIn = koinInject<IsLoggedInUseCase>()
    var loginPendingUri by remember { mutableStateOf<String?>(null) }
    DisposableEffect(navController) {
        DeepLinkLauncher.attach(
            navController = navController,
            isLoggedIn = { isLoggedIn() },
            openLoginListener = { uri -> loginPendingUri = uri },
        )
        onDispose { DeepLinkLauncher.detach() }
    }
    val currentEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentEntry) { DeepLinkLauncher.onNavStateChanged() }

    loginPendingUri?.let { pendingUri ->
        AuthBottomSheet(
            onAuthenticated = {
                loginPendingUri = null
                DeepLinkLauncher.enqueue(pendingUri)
            },
            onDismiss = { loginPendingUri = null },
        )
    }

    NavHost(navController = navController, startDestination = Screen.Splash) {
        composable<Screen.Splash> {
            SplashRoute(navController = navController, viewModel = koinViewModel())
        }
        composable<Screen.Onboarding> {
            OnboardingRoute(navController = navController, viewModel = koinViewModel())
        }
        composable<Screen.Main> { entry ->
            MainScreen(
                rootNavController = navController,
                savedStateHandle = entry.navBackStackEntry.savedStateHandle,
            )
        }
        composable<Screen.Product> { entry ->
            ProductRoute(navController = navController, slug = entry.args.slug)
        }
        composable<Screen.Account> {
            AccountRoute(navController = navController)
        }
        composable<Screen.Orders> {
            OrdersRoute(navController = navController)
        }
        composable<Screen.OrderDetail> { entry ->
            OrderDetailRoute(navController = navController, orderId = entry.args.orderId)
        }
    }
}
