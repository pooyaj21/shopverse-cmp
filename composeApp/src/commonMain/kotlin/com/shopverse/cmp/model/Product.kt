package com.shopverse.cmp.model

data class Product(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val developer: String,
    val publisher: String,
    val genre: String,
    val platforms: List<String>,
    val releaseDate: String,
    val currentPrice: Double,
    val oldPrice: Double?,
    val currency: String,
    val image: String,
    val stock: Int?,
    val ratingAvg: Double?,
    val ratingCount: Int?,
) {
    val isOnSale: Boolean get() = oldPrice != null && oldPrice > currentPrice
}
