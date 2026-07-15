package com.shopverse.cmp.network.converter

import com.shopverse.cmp.model.OrderDetail
import com.shopverse.cmp.model.OrderLineItem
import com.shopverse.cmp.model.OrderSummary
import com.shopverse.cmp.network.model.response.OrderDetailResponse
import com.shopverse.cmp.network.model.response.OrderItemResponse
import com.shopverse.cmp.network.model.response.OrderSummaryResponse

fun OrderSummaryResponse.toDomain(): OrderSummary = OrderSummary(
    id = id,
    placedAt = placedAt,
    total = total,
    originalTotal = originalTotal,
    currency = currency ?: "USD",
)

fun OrderDetailResponse.toDomain(): OrderDetail = OrderDetail(
    id = id,
    placedAt = placedAt,
    total = total,
    originalTotal = originalTotal,
    currency = currency ?: "USD",
    items = items.map { it.toDomain() },
)

fun OrderItemResponse.toDomain(): OrderLineItem = OrderLineItem(
    id = id,
    productId = productId,
    productSlug = productSlug,
    productTitle = productTitle,
    productImage = product?.image,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal,
)
