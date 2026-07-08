package com.shopverse.cmp.network.service.util

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.patch
import io.ktor.client.statement.HttpResponse
import io.ktor.http.encodedPath
import io.ktor.http.takeFrom

/**
 * PostgREST / Auth / Edge-function request helpers. `route` is appended to the Supabase base
 * URL (e.g. "/rest/v1/products", "/auth/v1/token"). These mirror ProvinCompose's getRequest /
 * postRequest but target Supabase's path layout.
 */

suspend inline fun <reified T> HttpClient.getRequest(
    route: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): T = get {
    url { takeFrom(BASE_URL); encodedPath += route }
    block()
}.body()

suspend inline fun <reified T> HttpClient.postRequest(
    route: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): T = post {
    url { takeFrom(BASE_URL); encodedPath += route }
    block()
}.body()

suspend inline fun <reified T> HttpClient.patchRequest(
    route: String,
    noinline block: HttpRequestBuilder.() -> Unit = {},
): T = patch {
    url { takeFrom(BASE_URL); encodedPath += route }
    block()
}.body()

/** Raw response variant — used when we must read the `Content-Range` header for pagination. */
suspend fun HttpClient.getRaw(
    route: String,
    block: HttpRequestBuilder.() -> Unit = {},
): HttpResponse = get {
    url { takeFrom(BASE_URL); encodedPath += route }
    block()
}

/**
 * Parse PostgREST's `Content-Range: <start>-<end>/<total>` header into the total row count.
 * Returns null when the header is absent or `*` (count not requested).
 */
fun parseContentRangeTotal(contentRange: String?): Int? {
    val total = contentRange?.substringAfter('/', "")?.trim().orEmpty()
    return total.toIntOrNull()
}

/** Adds the PostgREST exact-count preference so the response carries `Content-Range`. */
fun HttpRequestBuilder.preferExactCount() {
    header("Prefer", "count=exact")
}
