package com.shopverse.cmp.screen.product

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

/**
 * Placeholder product detail. The real screen (cover art, price w/ strikethrough, platforms,
 * rating, add-to-cart) lands in the week-4 milestone; wiring GetProductUseCase + a ViewModel
 * follows the exact Home pattern.
 */
@Composable
fun ProductRoute(
    navController: NavHostController,
    slug: String,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Product: $slug")
    }
}
