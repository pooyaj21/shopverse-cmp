package com.shopverse.cmp.screen.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopverse.cmp.core.architecture.Route
import com.shopverse.cmp.core.theme.PriceMuted
import com.shopverse.cmp.core.theme.TextGray
import com.shopverse.cmp.model.OrderSummary
import com.shopverse.cmp.screen.Screen
import com.shopverse.cmp.screen.component.BackButton
import com.shopverse.cmp.screen.component.ScreenTitle
import com.shopverse.cmp.screen.component.orderDateLabel
import com.shopverse.cmp.screen.component.orderNumberLabel
import com.shopverse.cmp.screen.component.orderPriceLabel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Order history, ported from the Android app's OrdersView: rounded cards with the short order
 * number + date on the left and total (with strikethrough original when discounted) on the
 * right, appending the next page as the list scrolls near its end.
 */
@Composable
fun OrdersRoute(navController: NavHostController) {
    val viewModel = koinViewModel<OrdersViewModel>()
    Route(
        viewModel = viewModel,
        topBar = {
            Column {
                BackButton(onClick = { navController.popBackStack() })
                ScreenTitle("Orders")
            }
        },
        onRetry = viewModel::refresh,
    ) { model ->
        if (model.items.isEmpty()) {
            Text(
                text = "No orders yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextGray,
                modifier = Modifier.align(Alignment.Center),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(model.items, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onClick = { navController.navigate(Screen.OrderDetail(order.id)) },
                    )
                }
                if (model.hasMore) {
                    item {
                        // Reaching this footer = scrolled near the end -> fetch the next page.
                        LaunchedEffect(Unit) { viewModel.loadMore() }
                        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp).align(Alignment.Center),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: OrderSummary, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp),
        ) {
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "Order ${orderNumberLabel(order.id)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    text = orderDateLabel(order.placedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = orderPriceLabel(order.total, order.currency),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                val originalTotal = order.originalTotal
                if (originalTotal != null && originalTotal > order.total) {
                    Text(
                        text = orderPriceLabel(originalTotal, order.currency),
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceMuted,
                        textDecoration = TextDecoration.LineThrough,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
