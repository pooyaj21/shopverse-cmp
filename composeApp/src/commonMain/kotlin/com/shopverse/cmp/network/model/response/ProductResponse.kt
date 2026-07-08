package com.shopverse.cmp.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * PostgREST row for `products`. Numeric columns arrive as JSON strings ("69.99") — the mapper
 * parses them to Double. snake_case matches the DB columns.
 */
@Serializable
data class ProductResponse(
    @SerialName("id") val id: String,
    @SerialName("slug") val slug: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("developer") val developer: String? = null,
    @SerialName("publisher") val publisher: String? = null,
    @SerialName("genre") val genre: String? = null,
    @SerialName("platforms") val platforms: List<String> = emptyList(),
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("current_price") val currentPrice: String,
    @SerialName("old_price") val oldPrice: String? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("stock") val stock: Int? = null,
    @SerialName("is_featured") val isFeatured: Boolean = false,
    @SerialName("is_trending") val isTrending: Boolean = false,
    @SerialName("rating_avg") val ratingAvg: String? = null,
    @SerialName("rating_count") val ratingCount: Int? = null,
)
