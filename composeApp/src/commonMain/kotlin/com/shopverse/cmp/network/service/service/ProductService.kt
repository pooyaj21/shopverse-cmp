package com.shopverse.cmp.network.service.service

import com.shopverse.cmp.network.model.response.ProductResponse
import com.shopverse.cmp.network.service.util.getRaw
import com.shopverse.cmp.network.service.util.getRequest
import com.shopverse.cmp.network.service.util.jsonFormatter
import com.shopverse.cmp.network.service.util.parseContentRangeTotal
import com.shopverse.cmp.network.service.util.preferExactCount
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders

/** A page of PostgREST rows plus the total count read from the `Content-Range` header. */
data class PagedRows<T>(val items: List<T>, val total: Int)

interface ProductService {
    suspend fun getProducts(limit: Int, offset: Int): PagedRows<ProductResponse>
    suspend fun getProducts(limit: Int, offset: Int, featured: Boolean?, trending: Boolean?): PagedRows<ProductResponse>
    suspend fun getBySlug(slug: String): List<ProductResponse>
}

class ProductServiceImpl(
    private val client: HttpClient,
) : ProductService {

    override suspend fun getProducts(limit: Int, offset: Int): PagedRows<ProductResponse> =
        getProducts(limit, offset, featured = null, trending = null)

    override suspend fun getProducts(
        limit: Int,
        offset: Int,
        featured: Boolean?,
        trending: Boolean?,
    ): PagedRows<ProductResponse> {
        val response = client.getRaw("/rest/v1/products") {
            parameter("select", "*")
            parameter("order", "release_date.desc")
            parameter("limit", limit)
            parameter("offset", offset)
            featured?.let { parameter("is_featured", "eq.$it") }
            trending?.let { parameter("is_trending", "eq.$it") }
            preferExactCount()
        }
        val items = jsonFormatter.decodeFromString<List<ProductResponse>>(response.bodyAsText())
        val total = parseContentRangeTotal(response.headers[HttpHeaders.ContentRange]) ?: items.size
        return PagedRows(items, total)
    }

    override suspend fun getBySlug(slug: String): List<ProductResponse> =
        client.getRequest("/rest/v1/products") {
            parameter("select", "*")
            parameter("slug", "eq.$slug")
        }
}
