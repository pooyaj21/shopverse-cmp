package com.shopverse.cmp.model

/**
 * A cart line. The cart is fully client-local (Room) — it never touches the server. Only
 * `{ productId, quantity }` is sent at checkout; the server re-prices everything.
 */
data class LocalCartItem(
    val id: String,
    val slug: String,
    val title: String,
    val currentPrice: Double,
    val oldPrice: Double?,
    val currency: String,
    val image: String,
    val count: Int,
) {
    val lineTotal: Double get() = currentPrice * count
}
