package com.shopverse.cmp.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One row of `GET /rest/v1/orders` (list projection — no line items). */
@Serializable
data class OrderSummaryResponse(
    @SerialName("id") val id: String,
    @SerialName("placed_at") val placedAt: String,
    @SerialName("total") val total: Double,
    @SerialName("original_total") val originalTotal: Double? = null,
    @SerialName("currency") val currency: String? = null,
)

/** `GET /rest/v1/orders?id=eq.<uuid>` with the `order_items(...)` join. */
@Serializable
data class OrderDetailResponse(
    @SerialName("id") val id: String,
    @SerialName("placed_at") val placedAt: String,
    @SerialName("total") val total: Double,
    @SerialName("original_total") val originalTotal: Double? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("order_items") val items: List<OrderItemResponse> = emptyList(),
)

@Serializable
data class OrderItemResponse(
    @SerialName("id") val id: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("product_slug") val productSlug: String,
    @SerialName("product_title") val productTitle: String,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("quantity") val quantity: Int,
    @SerialName("line_total") val lineTotal: Double,
    // Embedded live catalog row — null when the product was deleted after purchase.
    @SerialName("products") val product: EmbeddedProduct? = null,
) {
    @Serializable
    data class EmbeddedProduct(
        @SerialName("cover_image_url") val image: String? = null,
    )
}

/**
 * `data` payload of the submit-order envelope. Like the Android app we only keep the id and
 * route to the detail screen, which refetches the order — the QR is regenerated locally from
 * the deeplink, so the response's `qrCodeDataUrl`/`deeplink`/totals aren't needed.
 */
@Serializable
data class SubmittedOrderResponse(
    @SerialName("id") val id: String,
    @SerialName("placedAt") val placedAt: String? = null,
)
