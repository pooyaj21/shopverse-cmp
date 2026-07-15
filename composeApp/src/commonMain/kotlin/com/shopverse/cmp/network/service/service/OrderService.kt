package com.shopverse.cmp.network.service.service

import com.shopverse.cmp.network.model.base.BaseResponse
import com.shopverse.cmp.network.model.request.SubmitOrderRequest
import com.shopverse.cmp.network.model.response.OrderDetailResponse
import com.shopverse.cmp.network.model.response.OrderSummaryResponse
import com.shopverse.cmp.network.model.response.SubmittedOrderResponse
import com.shopverse.cmp.network.service.util.getRaw
import com.shopverse.cmp.network.service.util.getRequest
import com.shopverse.cmp.network.service.util.jsonFormatter
import com.shopverse.cmp.network.service.util.parseContentRangeTotal
import com.shopverse.cmp.network.service.util.postRequest
import com.shopverse.cmp.network.service.util.preferExactCount
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders

interface OrderService {
    suspend fun submit(request: SubmitOrderRequest): BaseResponse<SubmittedOrderResponse>
    suspend fun list(limit: Int, offset: Int): PagedRows<OrderSummaryResponse>
    suspend fun getById(id: String): OrderDetailResponse
}

class OrderServiceImpl(
    private val client: HttpClient,
) : OrderService {

    override suspend fun submit(request: SubmitOrderRequest): BaseResponse<SubmittedOrderResponse> =
        client.postRequest("/functions/v1/submit-order") { setBody(request) }

    /** Scoped to the caller by RLS; a logged-out user just gets `[]`. Newest first. */
    override suspend fun list(limit: Int, offset: Int): PagedRows<OrderSummaryResponse> {
        val response = client.getRaw("/rest/v1/orders") {
            parameter("select", "id,placed_at,total,original_total,currency")
            parameter("order", "placed_at.desc")
            parameter("limit", limit)
            parameter("offset", offset)
            preferExactCount()
        }
        val items = jsonFormatter.decodeFromString<List<OrderSummaryResponse>>(response.bodyAsText())
        val total = parseContentRangeTotal(response.headers[HttpHeaders.ContentRange]) ?: items.size
        return PagedRows(items, total)
    }

    override suspend fun getById(id: String): OrderDetailResponse =
        client.getRequest("/rest/v1/orders") {
            parameter("id", "eq.$id")
            parameter(
                "select",
                "id,placed_at,total,original_total,currency," +
                    "order_items(id,product_id,product_slug,product_title,unit_price,quantity,line_total," +
                    "products(cover_image_url))",
            )
            // Single-object response: PostgREST errors (406) when no row matches the id,
            // which safeApiCall surfaces as a remote error instead of an empty list.
            header(HttpHeaders.Accept, "application/vnd.pgrst.object+json")
        }
}
