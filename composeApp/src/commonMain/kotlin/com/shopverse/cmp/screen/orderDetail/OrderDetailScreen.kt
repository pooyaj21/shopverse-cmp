package com.shopverse.cmp.screen.orderDetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.shopverse.cmp.core.theme.DiscountRed
import com.shopverse.cmp.core.theme.PriceMuted
import com.shopverse.cmp.core.theme.TextGray
import com.shopverse.cmp.model.OrderLineItem
import com.shopverse.cmp.screen.Screen
import com.shopverse.cmp.screen.component.BackButton
import com.shopverse.cmp.screen.component.ScreenTitle
import com.shopverse.cmp.screen.component.orderDateLabel
import com.shopverse.cmp.screen.component.orderNumberLabel
import com.shopverse.cmp.screen.component.orderPriceLabel
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Order detail, ported from the Android app's OrderDetailView: header card (order number +
 * placed date), line-item card, summary card (strikethrough original / "You saved" / total),
 * and a QR receipt encoding `shopverse://orders/<id>` — rendered locally with qrose, exactly
 * like Android regenerates it with ZXing. Black-on-white regardless of theme: scanners first.
 */
@Composable
fun OrderDetailRoute(
    navController: NavHostController,
    orderId: String,
) {
    val viewModel = koinViewModel<OrderDetailViewModel> { parametersOf(orderId) }
    Route(
        viewModel = viewModel,
        topBar = {
            Column {
                BackButton(onClick = { navController.popBackStack() })
                ScreenTitle("Order Details")
            }
        },
        onRetry = viewModel::loadOrder,
        onEffect = { effect ->
            when (effect) {
                is OrderDetailEffect.OpenProduct ->
                    navController.navigate(Screen.Product(effect.slug))
            }
        },
    ) { model ->
        val order = model.order
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
        ) {
            Card(modifier = Modifier.padding(top = 12.dp)) {
                InfoRow(label = "Order", value = orderNumberLabel(order.id))
                CardDivider()
                InfoRow(label = "Placed", value = orderDateLabel(order.placedAt, withTime = true))
            }

            if (order.items.isNotEmpty()) {
                SectionTitle("Items")
                Card {
                    order.items.forEachIndexed { index, item ->
                        if (index > 0) CardDivider()
                        LineItemRow(
                            item = item,
                            currency = order.currency,
                            onClick = { viewModel.onItemClick(item.productSlug) },
                        )
                    }
                }
            }

            SectionTitle("Summary")
            Card {
                val savings = model.savings
                if (savings != null) {
                    InfoRow(
                        label = "Original total",
                        value = orderPriceLabel(order.originalTotal!!, order.currency),
                        valueColor = PriceMuted,
                        strikeThrough = true,
                    )
                    CardDivider()
                    InfoRow(
                        label = "You saved",
                        value = orderPriceLabel(savings, order.currency),
                        valueColor = DiscountRed,
                    )
                    CardDivider()
                }
                InfoRow(
                    label = "Total",
                    value = orderPriceLabel(order.total, order.currency),
                    emphasized = true,
                )
            }

            SectionTitle("Receipt")
            Card(modifier = Modifier.padding(bottom = 12.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                ) {
                    Image(
                        painter = rememberQrCodePainter(model.deeplink),
                        contentDescription = "Order receipt QR code",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Card(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        content()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun CardDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(start = 16.dp),
    )
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    strikeThrough: Boolean = false,
    emphasized: Boolean = false,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Normal,
            color = if (emphasized) MaterialTheme.colorScheme.onSurface else TextGray,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(end = 16.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Medium,
            color = valueColor.takeIf { it != Color.Unspecified }
                ?: MaterialTheme.colorScheme.onSurface,
            textDecoration = if (strikeThrough) TextDecoration.LineThrough else null,
            maxLines = 1,
        )
    }
}

@Composable
private fun LineItemRow(
    item: OrderLineItem,
    currency: String,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        // Steam cover art is 600x900 portrait — keep the 2:3 ratio.
        AsyncImage(
            model = item.productImage,
            contentDescription = item.productTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(40.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                text = item.productTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${item.quantity} × ${orderPriceLabel(item.unitPrice, currency)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(
            text = orderPriceLabel(item.lineTotal, currency),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}
