package com.shopverse.cmp.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CartItem")
data class CartItemEntity(
    @PrimaryKey @ColumnInfo(name = "product_id") val productId: String,
    @ColumnInfo(name = "slug") val slug: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "current_price") val currentPrice: Double,
    @ColumnInfo(name = "old_price") val oldPrice: Double?,
    @ColumnInfo(name = "currency") val currency: String,
    @ColumnInfo(name = "image") val image: String,
    @ColumnInfo(name = "count") val count: Int,
)
