package com.shopverse.cmp.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shopverse.cmp.core.theme.AddToCartBlue
import com.shopverse.cmp.core.theme.DiscountRed
import com.shopverse.cmp.core.theme.PriceMuted
import com.shopverse.cmp.core.theme.RatingStar
import com.shopverse.cmp.model.Product
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.painterResource
import shopversecmp.composeapp.generated.resources.Res
import shopversecmp.composeapp.generated.resources.ic_add
import shopversecmp.composeapp.generated.resources.ic_star

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick,
    ) {
        Column(Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)),
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                )
                if (product.isOnSale) {
                    DiscountBadge(
                        percent = discountPercent(product.oldPrice!!, product.currentPrice),
                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(AddToCartBlue, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = "Add to cart",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = product.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${priceLabel(product.currentPrice)} ${product.currency}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                if (product.isOnSale) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = priceLabel(product.oldPrice!!),
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceMuted,
                        textDecoration = TextDecoration.LineThrough,
                        maxLines = 1,
                    )
                }
            }

            product.ratingAvg?.let { rating ->
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_star),
                        contentDescription = null,
                        tint = RatingStar,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = ratingLabel(rating),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val count = product.ratingCount ?: 0
                    if (count > 0) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "($count)",
                            style = MaterialTheme.typography.bodySmall,
                            color = PriceMuted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscountBadge(percent: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(DiscountRed, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = "-$percent%",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun discountPercent(oldPrice: Double, currentPrice: Double): Int =
    (((oldPrice - currentPrice) / oldPrice) * 100).roundToInt()

private fun priceLabel(value: Double): String {
    val cents = (value * 100).roundToInt()
    val whole = cents / 100
    val rem = cents % 100
    return if (rem == 0) whole.toString() else "$whole.${rem.toString().padStart(2, '0')}"
}

private fun ratingLabel(value: Double): String {
    val tenths = (value * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}
