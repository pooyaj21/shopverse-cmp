package com.shopverse.cmp.core.cart

import com.shopverse.cmp.database.dao.CartItemDAO
import com.shopverse.cmp.database.model.CartItemEntity
import com.shopverse.cmp.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Single owner of the client-local cart, mirroring the Android app's CartManager. Unlike the
 * Android version there is no init()/in-memory map: Room's observable queries make the DAO the
 * source of truth, so flows stay correct without manual publishing.
 */
class CartManager(private val cartDao: CartItemDAO) {

    val idsFlow: Flow<Set<String>> = cartDao.observeAll()
        .map { items -> items.map { it.productId }.toSet() }
        .distinctUntilChanged()

    /** Adds [product] with count 1. No-op when already in the cart (same as Android). */
    suspend fun add(product: Product) {
        if (cartDao.selectAll().any { it.productId == product.id }) return
        cartDao.insertOrUpdate(product.toCartItem())
    }

    suspend fun remove(productId: String) = cartDao.delete(productId)

    suspend fun clear() = cartDao.deleteAll()

    private fun Product.toCartItem(): CartItemEntity = CartItemEntity(
        productId = id,
        slug = slug,
        title = title,
        currentPrice = currentPrice,
        oldPrice = oldPrice,
        currency = currency,
        image = image,
        count = 1,
    )
}
