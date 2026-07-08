package com.shopverse.cmp.screen.home

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModelState
import com.shopverse.cmp.model.PagedResult
import com.shopverse.cmp.model.Product
import com.shopverse.cmp.network.service.util.fold
import com.shopverse.cmp.network.useCase.GetProductsUseCase
import kotlinx.coroutines.launch

data class HomeModel(
    val featured: List<Product>,
    val catalog: List<Product>,
)

sealed interface HomeEffect {
    data class OpenProduct(val slug: String) : HomeEffect
}

class HomeViewModel(
    private val getProducts: GetProductsUseCase,
) : BaseViewModelState<HomeModel, HomeEffect>() {

    init {
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
}
