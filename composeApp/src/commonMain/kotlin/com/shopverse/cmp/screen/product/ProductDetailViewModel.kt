package com.shopverse.cmp.screen.product

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModelState
import com.shopverse.cmp.core.cart.CartManager
import com.shopverse.cmp.model.Product
import com.shopverse.cmp.network.service.util.fold
import com.shopverse.cmp.network.useCase.GetProductUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class ProductDetailModel(
    val product: Product,
    val isInCart: Boolean,
) {
    val isOutOfStock: Boolean get() = product.stock != null && product.stock <= 0
}

sealed interface ProductDetailEffect {
    data object GoToCart : ProductDetailEffect
}

class ProductDetailViewModel(
    private val slug: String,
    private val getProduct: GetProductUseCase,
    private val cartManager: CartManager,
) : BaseViewModelState<ProductDetailModel, ProductDetailEffect>() {

    private var product: Product? = null
    private var cartIds: Set<String> = emptySet()

    init {
        load()
        cartManager.idsFlow
            .onEach { ids ->
                cartIds = ids
                val loaded = product ?: return@onEach
                setSuccessState(ProductDetailModel(product = loaded, isInCart = loaded.id in ids))
            }
            .launchIn(viewModelScope)
    }

    fun load() {
        setLoadingState()
        viewModelScope.launch {
            getProduct(slug).fold(
                onSuccess = { loaded ->
                    if (loaded == null) {
                        setErrorState("Product not found")
                    } else {
                        product = loaded
                        setSuccessState(
                            ProductDetailModel(product = loaded, isInCart = loaded.id in cartIds),
                        )
                    }
                },
                onError = { _, message, _ ->
                    setErrorState(message ?: "Couldn't load the product")
                },
            )
        }
    }

    fun addToCart() {
        viewModelScope.launch { product?.let { cartManager.add(it) } }
    }

    fun goToCart() {
        viewModelScope.launch { sendEffect(ProductDetailEffect.GoToCart) }
    }
}
