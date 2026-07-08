package com.shopverse.cmp.network.model.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The `{ data, meta }` envelope returned by edge functions (`submit-order`, `delete-account`).
 * PostgREST endpoints do NOT use this — they return naked JSON — so only edge-function DTOs
 * are wrapped in [BaseResponse.Data].
 */
@Serializable
data class BaseResponse<T>(
    @SerialName("data") val data: T? = null,
    @SerialName("meta") val meta: MetaResponse = MetaResponse(),
)

@Serializable
data class MetaResponse(
    @SerialName("code") val code: Int = 0,
    @SerialName("msg") val message: String? = null,
)
