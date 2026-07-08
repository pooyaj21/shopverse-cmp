package com.shopverse.cmp.network.converter

import com.shopverse.cmp.model.Product
import com.shopverse.cmp.network.model.response.ProductResponse

fun ProductResponse.toDomain(): Product = Product(
    id = id,
    slug = slug,
    title = title,
    description = description.orEmpty(),
    developer = developer.orEmpty(),
    publisher = publisher.orEmpty(),
    genre = genre.orEmpty(),
    platforms = platforms,
    releaseDate = releaseDate.orEmpty(),
    currentPrice = currentPrice.toDoubleOrNull() ?: 0.0,
    oldPrice = oldPrice?.toDoubleOrNull(),
    currency = currency ?: "USD",
    image = coverImageUrl.orEmpty(),
    stock = stock,
    ratingAvg = ratingAvg?.toDoubleOrNull(),
    ratingCount = ratingCount,
)

fun List<ProductResponse>.toDomain(): List<Product> = map { it.toDomain() }
