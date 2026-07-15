package com.shopverse.cmp.network.useCase

import com.shopverse.cmp.model.LocalCartItem
import com.shopverse.cmp.model.OrderDetail
import com.shopverse.cmp.model.OrderSummary
import com.shopverse.cmp.model.PagedResult
import com.shopverse.cmp.network.repository.OrderRepository
import com.shopverse.cmp.network.service.util.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface SubmitOrderUseCase : UseCase {
    /** Returns the new order's id. */
    suspend operator fun invoke(items: List<LocalCartItem>): AppResult<String>
}

class SubmitOrderUseCaseImpl(
    private val orderRepository: OrderRepository,
) : SubmitOrderUseCase {
    override suspend fun invoke(items: List<LocalCartItem>): AppResult<String> =
        withContext(Dispatchers.Default) { orderRepository.submit(items) }
}

interface GetOrdersUseCase : UseCase {
    suspend operator fun invoke(
        limit: Int = PagedResult.DEFAULT_PAGE_SIZE,
        offset: Int = 0,
    ): AppResult<PagedResult<OrderSummary>>
}

class GetOrdersUseCaseImpl(
    private val orderRepository: OrderRepository,
) : GetOrdersUseCase {
    override suspend fun invoke(limit: Int, offset: Int): AppResult<PagedResult<OrderSummary>> =
        withContext(Dispatchers.Default) { orderRepository.getOrders(limit, offset) }
}

interface GetOrderUseCase : UseCase {
    suspend operator fun invoke(orderId: String): AppResult<OrderDetail>
}

class GetOrderUseCaseImpl(
    private val orderRepository: OrderRepository,
) : GetOrderUseCase {
    override suspend fun invoke(orderId: String): AppResult<OrderDetail> =
        withContext(Dispatchers.Default) { orderRepository.getOrder(orderId) }
}
