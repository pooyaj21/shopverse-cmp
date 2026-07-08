package com.shopverse.cmp.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.shopverse.cmp.core.architecture.Route
import com.shopverse.cmp.model.Product
import com.shopverse.cmp.screen.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    navController: NavHostController,
    viewModel: HomeViewModel,
) {
    Route(
        viewModel = viewModel,
        topBar = { TopAppBar(title = { Text("ShopVerse") }) },
        onRetry = viewModel::load,
        onEffect = { effect ->
            when (effect) {
                is HomeEffect.OpenProduct -> navController.navigate(Screen.Product(effect.slug))
            }
        },
    ) { model ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
        ) {
            items(model.catalog, key = { it.id }) { product ->
                ProductCard(product = product, onClick = { viewModel.onProductClick(product) })
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), onClick = onClick) {
        Column {
            AsyncImage(
                model = product.image,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(180.dp),
            )
            Column(Modifier.padding(8.dp)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${product.currency} ${product.currentPrice}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
