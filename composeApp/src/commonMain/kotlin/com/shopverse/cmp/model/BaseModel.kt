package com.shopverse.cmp.model

/**
 * Domain envelope for edge-function responses (`submit-order`, `delete-account`) which wrap
 * their payload in `{ data, meta }`. PostgREST calls skip this and return the naked model.
 */
sealed class BaseModel {

    abstract val meta: Meta

    data class MetaOnly(override val meta: Meta) : BaseModel()

    data class Data<T>(override val meta: Meta, val data: T) : BaseModel()
}
