package com.shopverse.cmp.network.service.util

import kotlinx.serialization.json.Json

/** Lenient JSON used by both the Ktor ContentNegotiation plugin and manual error parsing. */
val jsonFormatter: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    coerceInputValues = true
}
