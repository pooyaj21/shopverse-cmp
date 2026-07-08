package com.shopverse.cmp.screen.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.shopverse.cmp.screen.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashRoute(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(1200)
        navController.navigate(Screen.Home) {
            popUpTo(Screen.Splash) { inclusive = true }
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("ShopVerse", style = MaterialTheme.typography.headlineLarge)
    }
}
