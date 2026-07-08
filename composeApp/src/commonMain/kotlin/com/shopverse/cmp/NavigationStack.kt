package com.shopverse.cmp

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.shopverse.cmp.core.architecture.navigation.composable
import com.shopverse.cmp.screen.Screen
import com.shopverse.cmp.screen.main.MainScreen
import com.shopverse.cmp.screen.onboarding.OnboardingRoute
import com.shopverse.cmp.screen.product.ProductRoute
import com.shopverse.cmp.screen.splash.SplashRoute
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavigationStack(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Splash) {
        composable<Screen.Splash> {
            SplashRoute(navController = navController, viewModel = koinViewModel())
        }
        composable<Screen.Onboarding> {
            OnboardingRoute(navController = navController, viewModel = koinViewModel())
        }
        composable<Screen.Main> {
            MainScreen(rootNavController = navController)
        }
        composable<Screen.Product> { entry ->
            ProductRoute(navController = navController, slug = entry.args.slug)
        }
    }
}
