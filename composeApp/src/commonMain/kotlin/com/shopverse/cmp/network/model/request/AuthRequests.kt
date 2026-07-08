package com.shopverse.cmp.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
)

@Serializable
data class SignUpRequest(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
    // Flows into the profiles row via the on_auth_user_created trigger.
    @SerialName("data") val data: SignUpData,
) {
    @Serializable
    data class SignUpData(@SerialName("name") val name: String)
}

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String,
)
