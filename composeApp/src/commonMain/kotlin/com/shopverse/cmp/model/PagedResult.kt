package com.shopverse.cmp.model

/**
 * Client-side pagination window. [total] comes from PostgREST's `Content-Range` header
 * (e.g. `0-9/88`); [hasMore] is computed the same way the Android app computes `hasNext`.
 */
data class PagedResult<T>(
    val items: List<T>,
    val offset: Int,
    val limit: Int,
    val total: Int,
) {
    val hasMore: Boolean get() = offset + items.size < total

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }
}
