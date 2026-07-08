package com.shopverse.cmp.network.repository

import com.shopverse.cmp.model.PagedResult
import com.shopverse.cmp.model.Product
import com.shopverse.cmp.network.converter.toDomain
import com.shopverse.cmp.network.service.service.ProductService
import com.shopverse.cmp.network.service.util.AppResult
import com.shopverse.cmp.network.service.util.mapIfSuccess
import com.shopverse.cmp.network.service.util.safeApiCall

interface ProductRepository {
    suspend fun getProducts(
        limit: Int,
        offset: Int,
        featured: Boolean? = null,
        trending: Boolean? = null,
    ): AppResult<PagedResult<Product>>

    /** Null when the slug is unknown — PostgREST returns `[]`, never a 404. */
    suspend fun getProduct(slug: String): AppResult<Product?>
}

class ProductRepositoryImpl(
    private val productService: ProductService,
) : ProductRepository {

    override suspend fun getProducts(
        limit: Int,
        offset: Int,
        featured: Boolean?,
        trending: Boolean?,
    ): AppResult<PagedResult<Product>> = safeApiCall {
        productService.getProducts(limit, offset, featured, trending)
    }.mapIfSuccess { paged ->
        PagedResult(
            items = paged.items.toDomain(),
            offset = offset,
            limit = limit,
            total = paged.total,
        )
    }

    override suspend fun getProduct(slug: String): AppResult<Product?> = safeApiCall {
        productService.getBySlug(slug)
    }.mapIfSuccess { rows -> rows.firstOrNull()?.toDomain() }
}
