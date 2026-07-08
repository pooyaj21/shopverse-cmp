package com.shopverse.cmp.network.model.base

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A permissive union of the three error shapes the backend can return (see docs/API.md):
 *  - PostgREST: `{ code, message, details, hint }`
 *  - Auth:      `{ error, error_description }`  (also `{ msg }` / `error_code` on some paths)
 *  - Edge fn:   `{ data: null, meta: { code, msg } }`
 * Every field is optional so a single decode attempt works for all of them.
 */
@Serializable
data class ErrorResponse(
    @SerialName("message") val message: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
    @SerialName("error") val error: String? = null,
    @SerialName("msg") val msg: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("hint") val hint: String? = null,
    @SerialName("meta") val meta: MetaResponse? = null,
) {
    /** Best human-readable message across all shapes. */
    val bestMessage: String?
        get() = errorDescription ?: message ?: msg ?: meta?.message ?: error ?: errorCode ?: code
}
