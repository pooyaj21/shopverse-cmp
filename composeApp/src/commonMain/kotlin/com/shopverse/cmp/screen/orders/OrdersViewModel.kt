package com.shopverse.cmp.screen.orders

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModelState
import com.shopverse.cmp.model.OrderSummary
import com.shopverse.cmp.model.PagedResult
import com.shopverse.cmp.network.service.util.AppResult
import com.shopverse.cmp.network.useCase.GetOrdersUseCase
import kotlinx.coroutines.launch

data class OrdersModel(
    val items: List<OrderSummary>,
    val hasMore: Boolean,
)

/**
 * Order history ViewModel, ported from the Android app's OrdersViewModel: accumulates
 * limit/offset pages (total from `Content-Range`) and appends as the list scrolls near its end.
 */
class OrdersViewModel(
    private val getOrders: GetOrdersUseCase,
) : BaseViewModelState<OrdersModel, Unit>() {

    private val pageSize = PagedResult.DEFAULT_PAGE_SIZE
    private val accumulated = mutableListOf<OrderSummary>()
    private var nextOffset = 0
    private var total = Int.MAX_VALUE
    private var loading = false

    init {
        refresh()
    }

    fun refresh() {
        accumulated.clear()
        nextOffset = 0
        total = Int.MAX_VALUE
        loading = false
        loadMore()
    }

    fun loadMore() {
        if (loading || nextOffset >= total) return
        loading = true
        val isAppending = accumulated.isNotEmpty()

        viewModelScope.launch {
            try {
                if (!isAppending) setLoadingState()
                when (val result = getOrders(limit = pageSize, offset = nextOffset)) {
                    is AppResult.Success -> {
                        val page = result.value
                        accumulated.addAll(page.items)
                        nextOffset = page.offset + page.items.size
                        total = page.total
                        setSuccessState(
                            OrdersModel(items = accumulated.toList(), hasMore = nextOffset < total),
                        )
                    }
                    is AppResult.Error ->
                        // Append failures are silently dropped — the next scroll-near-end
                        // re-triggers. Same trade-off as the Android app.
                        if (!isAppending) setErrorState(result.message ?: "Couldn't load your orders")
                }
            } finally {
                loading = false
            }
        }
    }
}
