package com.shopverse.cmp.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.shopverse.cmp.core.theme.AddToCartBlue
import com.shopverse.cmp.core.theme.PriceMuted
import com.shopverse.cmp.core.theme.RatingStar
import com.shopverse.cmp.model.Product
import org.jetbrains.compose.resources.painterResource
import shopversecmp.composeapp.generated.resources.Res
import shopversecmp.composeapp.generated.resources.ic_add
import shopversecmp.composeapp.generated.resources.ic_cart
import shopversecmp.composeapp.generated.resources.ic_star

/** Catalog grid cell, ported from the Android app's ProductCellView. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    isInCart: Boolean,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onGoToCart: () -> Unit,
) {
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
                // Same behaviour as Android's AppQuantitySelector: "+" adds the product,
                // and once in the cart the icon flips to a cart that jumps to the Cart tab.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AddToCartBlue)
                        .clickable(onClick = if (isInCart) onGoToCart else onAddToCart),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(
                            if (isInCart) Res.drawable.ic_cart else Res.drawable.ic_add,
                        ),
                        contentDescription = if (isInCart) "Go to cart" else "Add to cart",
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
