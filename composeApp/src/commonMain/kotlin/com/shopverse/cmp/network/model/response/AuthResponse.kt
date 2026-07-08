package com.shopverse.cmp.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Shared shape of `/auth/v1/signup` and `/auth/v1/token` responses. */
@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    @SerialName("user") val user: AuthUser? = null,
)

@Serializable
data class AuthUser(
    @SerialName("id") val id: String,
    @SerialName("email") val email: String? = null,
    @SerialName("user_metadata") val userMetadata: UserMetadata? = null,
) {
    @Serializable
    data class UserMetadata(
        @SerialName("name") val name: String? = null,
    )
}
