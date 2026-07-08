package com.shopverse.cmp.model

data class OrderSummary(
    val id: String,
    val placedAt: String,
    val total: Double,
    val originalTotal: Double?,
    val currency: String,
)

data class OrderDetail(
    val id: String,
    val placedAt: String,
    val total: Double,
    val originalTotal: Double?,
    val currency: String,
    val items: List<OrderLineItem>,
    // Present only on the submit-order response (edge function): QR receipt + deeplink.
    val qrCodeDataUrl: String? = null,
    val deeplink: String? = null,
)

data class OrderLineItem(
    val id: String,
    val productId: String?,
    val productSlug: String,
    val productTitle: String,
    // Live catalog image — null when the product was deleted after purchase.
    val productImage: String?,
    val unitPrice: Double,
    val quantity: Int,
    val lineTotal: Double,
)
