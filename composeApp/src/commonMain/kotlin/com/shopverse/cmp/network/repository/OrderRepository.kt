package com.shopverse.cmp.network.repository

import com.shopverse.cmp.model.LocalCartItem
import com.shopverse.cmp.model.OrderDetail
import com.shopverse.cmp.model.OrderSummary
import com.shopverse.cmp.model.PagedResult
import com.shopverse.cmp.network.converter.toDomain
import com.shopverse.cmp.network.model.request.SubmitOrderRequest
import com.shopverse.cmp.network.service.service.OrderService
import com.shopverse.cmp.network.service.util.AppResult
import com.shopverse.cmp.network.service.util.mapIfSuccess
import com.shopverse.cmp.network.service.util.safeApiCall

interface OrderRepository {
    /** Returns the new order's id; the detail screen refetches the full order. */
    suspend fun submit(items: List<LocalCartItem>): AppResult<String>

    suspend fun getOrders(
        limit: Int = PagedResult.DEFAULT_PAGE_SIZE,
        offset: Int = 0,
    ): AppResult<PagedResult<OrderSummary>>

    /** Public by UUID — works logged-out (the QR/deeplink flow). */
    suspend fun getOrder(orderId: String): AppResult<OrderDetail>
}

class OrderRepositoryImpl(
    private val orderService: OrderService,
) : OrderRepository {

    override suspend fun submit(items: List<LocalCartItem>): AppResult<String> {
        val result = safeApiCall {
            orderService.submit(
                SubmitOrderRequest(
                    items = items.map { SubmitOrderRequest.Item(productId = it.id, quantity = it.count) },
                ),
            )
        }
        return when (result) {
            is AppResult.Success -> {
                val id = result.value.data?.id
                if (id == null) {
                    // 2xx envelope without data shouldn't happen; surface the meta message.
                    AppResult.Error.Remote(
                        httpCode = result.value.meta.code,
                        message = result.value.meta.message ?: "submit-order returned no data",
                        cause = null,
                    )
                } else {
                    AppResult.Success(id)
                }
            }
            is AppResult.Error -> result
        }
    }

    override suspend fun getOrders(
        limit: Int,
        offset: Int,
    ): AppResult<PagedResult<OrderSummary>> = safeApiCall {
        orderService.list(limit, offset)
    }.mapIfSuccess { paged ->
        PagedResult(
            items = paged.items.map { it.toDomain() },
            offset = offset,
            limit = limit,
            total = paged.total,
        )
    }

    override suspend fun getOrder(orderId: String): AppResult<OrderDetail> = safeApiCall {
        orderService.getById(orderId)
    }.mapIfSuccess { it.toDomain() }
}
