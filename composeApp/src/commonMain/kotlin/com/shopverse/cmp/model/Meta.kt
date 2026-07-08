package com.shopverse.cmp.model

/**
 * Domain-side mirror of the `{ meta: { code, msg } }` envelope shared by the Spring Boot
 * backend and the Supabase edge functions. PostgREST endpoints return naked JSON, so [Meta]
 * is only populated for edge-function responses.
 */
data class Meta(
    val code: Int,
    val message: String?,
)
