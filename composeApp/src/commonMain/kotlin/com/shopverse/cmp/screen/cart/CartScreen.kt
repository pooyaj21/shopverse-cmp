package com.shopverse.cmp.screen.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.shopverse.cmp.core.architecture.Route
import com.shopverse.cmp.core.theme.AddToCartBlue
import com.shopverse.cmp.core.theme.PriceMuted
import com.shopverse.cmp.core.theme.TextGray
import com.shopverse.cmp.model.LocalCartItem
import com.shopverse.cmp.screen.Screen
import com.shopverse.cmp.screen.component.ScreenTitle
import com.shopverse.cmp.screen.component.priceLabel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import shopversecmp.composeapp.generated.resources.Res
import shopversecmp.composeapp.generated.resources.ic_trash

/**
 * Cart, ported from the Android app's CartView: a list of line cards (art, title, price with
 * old-price strikethrough, circular trash button) over a Place Order button, with a centered
 * empty message when there's nothing in the cart. The cart is fully client-local.
 */
@Composable
fun CartRoute(navController: NavHostController) {
    val viewModel = koinViewModel<CartViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Route(
        viewModel = viewModel,
        topBar = { ScreenTitle("Cart") },
        onEffect = { effect ->
            when (effect) {
                is CartEffect.OpenProduct -> navController.navigate(Screen.Product(effect.slug))
                is CartEffect.ShowMessage ->
                    scope.launch { snackbarHostState.showSnackbar(effect.text) }
            }
        },
    ) { model ->
        if (model.items.isEmpty()) {
            Text(
                text = "Your cart is empty.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(model.items, key = { it.id }) { item ->
                        CartItemCard(
                            item = item,
                            onClick = { viewModel.onItemClick(item) },
                            onRemove = { viewModel.removeFromCart(item) },
                        )
                    }
                }
                Button(
                    onClick = viewModel::placeOrder,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AddToCartBlue,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                        .height(48.dp),
                ) {
                    Text(
                        text = "Place Order",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartItemCard(
    item: LocalCartItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                AsyncImage(
                    model = item.image,
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${priceLabel(item.currentPrice)} ${item.currency}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    val oldPrice = item.oldPrice
                    if (oldPrice != null && oldPrice > item.currentPrice) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "${priceLabel(oldPrice)} ${item.currency}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceMuted,
                            textDecoration = TextDecoration.LineThrough,
                            maxLines = 1,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_trash),
                    contentDescription = "Remove from cart",
                    tint = TextGray,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
