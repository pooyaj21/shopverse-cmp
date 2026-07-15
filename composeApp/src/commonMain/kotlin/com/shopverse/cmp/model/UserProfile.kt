package com.shopverse.cmp.model

import kotlinx.serialization.Serializable

// Serializable so SessionStore can persist it as JSON under Keys.PROFILE.
@Serializable
data class UserProfile(
    val id: String,
    val name: String?,
    val email: String?,
)
