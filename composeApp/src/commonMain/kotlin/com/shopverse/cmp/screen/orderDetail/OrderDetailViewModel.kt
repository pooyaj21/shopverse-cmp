package com.shopverse.cmp.screen.orderDetail

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModelState
import com.shopverse.cmp.model.OrderDetail
import com.shopverse.cmp.network.useCase.GetOrderUseCase
import com.shopverse.cmp.network.service.util.fold
import kotlinx.coroutines.launch

data class OrderDetailModel(
    val order: OrderDetail,
) {
    val savings: Double?
        get() = order.originalTotal
            ?.takeIf { it > order.total }
            ?.minus(order.total)

    /** Same format the backend's submit-order response uses; the QR encodes this. */
    val deeplink: String get() = "$DEEPLINK_SCHEME://orders/${order.id}"

    companion object {
        private const val DEEPLINK_SCHEME = "shopverse"
    }
}

sealed interface OrderDetailEffect {
    data class OpenProduct(val slug: String) : OrderDetailEffect
}

/** Loads one order by UUID — public by design, so this works logged-out (QR/deeplink flow). */
class OrderDetailViewModel(
    private val orderId: String,
    private val getOrder: GetOrderUseCase,
) : BaseViewModelState<OrderDetailModel, OrderDetailEffect>() {

    init {
        loadOrder()
    }

    fun loadOrder() {
        setLoadingState()
        viewModelScope.launch {
            getOrder(orderId).fold(
                onSuccess = { order -> setSuccessState(OrderDetailModel(order = order)) },
                onError = { httpCode, _, _ ->
                    setErrorState("Couldn't load this order${httpCode?.let { " ($it)" } ?: ""}.")
                },
            )
        }
    }

    fun onItemClick(slug: String) {
        viewModelScope.launch { sendEffect(OrderDetailEffect.OpenProduct(slug)) }
    }
}
