package com.shopverse.cmp.screen.cart

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModelState
import com.shopverse.cmp.core.cart.CartManager
import com.shopverse.cmp.model.LocalCartItem
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class CartModel(
    val items: List<LocalCartItem>,
)

sealed interface CartEffect {
    data class OpenProduct(val slug: String) : CartEffect
    data class ShowMessage(val text: String) : CartEffect
}

class CartViewModel(
    private val cartManager: CartManager,
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

    // TODO(week-5): call the submit-order edge function (needs the login flow first),
    // clear the cart on success, and navigate to the order confirmation + QR receipt.
    fun placeOrder() {
        viewModelScope.launch {
            sendEffect(CartEffect.ShowMessage("Checkout arrives with the orders feature."))
        }
    }
}
