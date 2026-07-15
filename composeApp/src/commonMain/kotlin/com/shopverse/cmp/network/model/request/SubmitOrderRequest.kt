package com.shopverse.cmp.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body of `POST /functions/v1/submit-order`. Only ids + quantities travel — each line is
 * priced server-side from the current catalog row, so a tampered client can't pay $0.
 */
@Serializable
data class SubmitOrderRequest(
    @SerialName("items") val items: List<Item>,
) {
    @Serializable
    data class Item(
        @SerialName("productId") val productId: String,
        @SerialName("quantity") val quantity: Int,
    )
}
