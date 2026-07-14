package com.shopverse.cmp.screen.home

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModelState
import com.shopverse.cmp.core.architecture.ViewState
import com.shopverse.cmp.core.cart.CartManager
import com.shopverse.cmp.model.PagedResult
import com.shopverse.cmp.model.Product
import com.shopverse.cmp.network.service.util.fold
import com.shopverse.cmp.network.useCase.GetProductsUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class HomeModel(
    val featured: List<Product>,
    val catalog: List<Product>,
    val cartIds: Set<String>,
)

sealed interface HomeEffect {
    data class OpenProduct(val slug: String) : HomeEffect
    data object OpenCart : HomeEffect
}

class HomeViewModel(
    private val getProducts: GetProductsUseCase,
    private val cartManager: CartManager,
) : BaseViewModelState<HomeModel, HomeEffect>() {

    private var cartIds: Set<String> = emptySet()

    init {
        cartManager.idsFlow
            .onEach { ids ->
                cartIds = ids
                val current = data
                if (state == ViewState.Success && current != null && current.cartIds != ids) {
                    setSuccessState(current.copy(cartIds = ids))
                }
            }
            .launchIn(viewModelScope)
        load()
    }

    fun load() {
        setLoadingState()
        viewModelScope.launch {
            val featured = getProducts(limit = 10, offset = 0, featured = true)
            val catalog = getProducts(limit = PagedResult.DEFAULT_PAGE_SIZE, offset = 0)

            catalog.fold(
                onSuccess = { page ->
                    setSuccessState(
                        HomeModel(
                            featured = featured.fold({ it.items }, { _, _, _ -> emptyList() }),
                            catalog = page.items,
                            cartIds = cartIds,
                        ),
                    )
                },
                onError = { _, message, _ ->
                    setErrorState(message ?: "Couldn't load the catalog")
                },
            )
        }
    }

    fun onProductClick(product: Product) {
        viewModelScope.launch { sendEffect(HomeEffect.OpenProduct(product.slug)) }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch { cartManager.add(product) }
    }

    fun onGoToCartClick() {
        viewModelScope.launch { sendEffect(HomeEffect.OpenCart) }
    }
}
