package com.shopverse.cmp.network.useCase

import com.shopverse.cmp.model.PagedResult
import com.shopverse.cmp.model.Product
import com.shopverse.cmp.network.repository.ProductRepository
import com.shopverse.cmp.network.service.util.AppResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface GetProductsUseCase : UseCase {
    suspend operator fun invoke(
        limit: Int = PagedResult.DEFAULT_PAGE_SIZE,
        offset: Int = 0,
        featured: Boolean? = null,
        trending: Boolean? = null,
    ): AppResult<PagedResult<Product>>
}

class GetProductsUseCaseImpl(
    private val productRepository: ProductRepository,
) : GetProductsUseCase {
    override suspend fun invoke(
        limit: Int,
        offset: Int,
        featured: Boolean?,
        trending: Boolean?,
    ): AppResult<PagedResult<Product>> = withContext(Dispatchers.Default) {
        productRepository.getProducts(limit, offset, featured, trending)
    }
}

interface GetProductUseCase : UseCase {
    suspend operator fun invoke(slug: String): AppResult<Product?>
}

class GetProductUseCaseImpl(
    private val productRepository: ProductRepository,
) : GetProductUseCase {
    override suspend fun invoke(slug: String): AppResult<Product?> =
        withContext(Dispatchers.Default) { productRepository.getProduct(slug) }
}
