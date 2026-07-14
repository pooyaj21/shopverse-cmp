package com.shopverse.cmp.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopverse.cmp.core.architecture.Route
import com.shopverse.cmp.screen.Screen
import com.shopverse.cmp.screen.component.ProductCard
import com.shopverse.cmp.screen.component.ScreenTitle
import com.shopverse.cmp.screen.main.MAIN_SELECT_TAB_CART
import com.shopverse.cmp.screen.main.MAIN_SELECT_TAB_KEY

@Composable
fun HomeRoute(
    navController: NavHostController,
    viewModel: HomeViewModel,
) {
    Route(
        viewModel = viewModel,
        topBar = { ScreenTitle("Home") },
        onRetry = viewModel::load,
        onEffect = { effect ->
            when (effect) {
                is HomeEffect.OpenProduct -> navController.navigate(Screen.Product(effect.slug))
                HomeEffect.OpenCart ->
                    // We're already inside Main's tab host — just ask it to select Cart.
                    runCatching { navController.getBackStackEntry(Screen.Main) }
                        .getOrNull()
                        ?.savedStateHandle
                        ?.set(MAIN_SELECT_TAB_KEY, MAIN_SELECT_TAB_CART)
            }
        },
    ) { model ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(model.catalog, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    isInCart = product.id in model.cartIds,
                    onClick = { viewModel.onProductClick(product) },
                    onAddToCart = { viewModel.addToCart(product) },
                    onGoToCart = viewModel::onGoToCartClick,
                )
            }
        }
    }
}
