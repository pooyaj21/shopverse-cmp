package com.shopverse.cmp.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shopverse.cmp.core.theme.DiscountRed
import kotlin.math.roundToInt

/** Product bits shared by the catalog card and the detail screen (Android parity). */

@Composable
fun DiscountBadge(percent: Int, modifier: Modifier = Modifier) {
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

fun discountPercent(oldPrice: Double, currentPrice: Double): Int =
    (((oldPrice - currentPrice) / oldPrice) * 100).roundToInt()

/** Whole prices render without decimals ("59"), fractional ones with two ("59.99"). */
fun priceLabel(value: Double): String {
    val cents = (value * 100).roundToInt()
    val whole = cents / 100
    val rem = cents % 100
    return if (rem == 0) whole.toString() else "$whole.${rem.toString().padStart(2, '0')}"
}

fun ratingLabel(value: Double): String {
    val tenths = (value * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}
