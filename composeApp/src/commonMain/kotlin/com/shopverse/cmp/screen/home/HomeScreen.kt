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
import com.shopverse.cmp.screen.component.ScreenTitle

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
                ProductCard(product = product, onClick = { viewModel.onProductClick(product) })
            }
        }
    }
}
