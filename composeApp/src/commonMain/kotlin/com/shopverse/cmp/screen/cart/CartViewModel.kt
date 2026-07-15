package com.shopverse.cmp.screen.cart

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModelState
import com.shopverse.cmp.core.cart.CartManager
import com.shopverse.cmp.model.LocalCartItem
import com.shopverse.cmp.network.service.util.AppResult
import com.shopverse.cmp.network.useCase.IsLoggedInUseCase
import com.shopverse.cmp.network.useCase.SubmitOrderUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class CartModel(
    val items: List<LocalCartItem>,
)

sealed interface CartEffect {
    data class OpenProduct(val slug: String) : CartEffect
    data class ShowMessage(val text: String) : CartEffect
    data object ShowLogin : CartEffect
    data class OrderPlaced(val orderId: String) : CartEffect
}

class CartViewModel(
    private val cartManager: CartManager,
    private val isLoggedIn: IsLoggedInUseCase,
    private val submitOrder: SubmitOrderUseCase,
) : BaseViewModelState<CartModel, CartEffect>() {

    init {
        cartManager.itemsFlow
            .onEach { items -> setSuccessState(CartModel(items = items)) }
            .launchIn(viewModelScope)
    }

    fun onItemClick(item: LocalCartItem) {
        viewModelScope.launch { sendEffect(CartEffect.OpenProduct(item.slug)) }
    }

    fun removeFromCart(item: LocalCartItem) {
        viewModelScope.launch { cartManager.remove(item.id) }
    }

    /** The Android `ensureUserLogin { placeOrder() }` gate: guests get the auth sheet first. */
    fun onPlaceOrderClick() {
        viewModelScope.launch {
            if (isLoggedIn()) placeOrder() else sendEffect(CartEffect.ShowLogin)
        }
    }

    fun placeOrder() {
        viewModelScope.launch {
            val items = data?.items.orEmpty()
            if (items.isEmpty()) {
                sendEffect(CartEffect.ShowMessage("Your cart is empty."))
                return@launch
            }
            setLoadingState()
            when (val result = submitOrder(items)) {
                is AppResult.Success -> {
                    // Clearing the cart re-emits through itemsFlow, which restores Success state.
                    cartManager.clear()
                    sendEffect(CartEffect.OrderPlaced(orderId = result.value))
                }
                is AppResult.Error.Local -> {
                    setSuccessState(CartModel(items = items))
                    sendEffect(CartEffect.ShowMessage("Network problem. Please try again."))
                }
                is AppResult.Error.Remote -> {
                    setSuccessState(CartModel(items = items))
                    sendEffect(CartEffect.ShowMessage(prettyRemoteError(result.httpCode, result.message)))
                }
            }
        }
    }

    private fun prettyRemoteError(httpCode: Int, message: String?): String = when {
        httpCode == 401 -> "Please log in to place an order."
        message.isNullOrBlank() -> "Something went wrong ($httpCode)."
        message.contains("insufficient_stock", ignoreCase = true) -> "Some items are out of stock."
        message.contains("product_not_found", ignoreCase = true) -> "A product is no longer available."
        else -> "Something went wrong ($httpCode)."
    }
}
