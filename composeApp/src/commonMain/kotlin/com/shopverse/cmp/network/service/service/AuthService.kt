package com.shopverse.cmp.network.service.service

import com.shopverse.cmp.network.model.base.BaseResponse
import com.shopverse.cmp.network.model.request.LoginRequest
import com.shopverse.cmp.network.model.request.RefreshTokenRequest
import com.shopverse.cmp.network.model.request.SignUpRequest
import com.shopverse.cmp.network.model.response.AuthResponse
import com.shopverse.cmp.network.model.response.AuthUser
import com.shopverse.cmp.network.model.response.DeleteAccountResponse
import com.shopverse.cmp.network.service.util.getRequest
import com.shopverse.cmp.network.service.util.postRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import kotlinx.serialization.json.JsonObject

interface AuthService {
    suspend fun signUp(request: SignUpRequest): AuthResponse
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun refresh(request: RefreshTokenRequest): AuthResponse
    suspend fun logout()
    suspend fun getUser(): AuthUser
    suspend fun deleteAccount(): BaseResponse<DeleteAccountResponse>
}

class AuthServiceImpl(
    private val client: HttpClient,
) : AuthService {

    override suspend fun signUp(request: SignUpRequest): AuthResponse =
        client.postRequest("/auth/v1/signup") { setBody(request) }

    override suspend fun login(request: LoginRequest): AuthResponse =
        client.postRequest("/auth/v1/token") {
            parameter("grant_type", "password")
            setBody(request)
        }

    override suspend fun refresh(request: RefreshTokenRequest): AuthResponse =
        client.postRequest("/auth/v1/token") {
            parameter("grant_type", "refresh_token")
            setBody(request)
        }

    override suspend fun logout() =
        client.postRequest<Unit>("/auth/v1/logout")

    override suspend fun getUser(): AuthUser =
        client.getRequest("/auth/v1/user")

    override suspend fun deleteAccount(): BaseResponse<DeleteAccountResponse> =
        // Edge function; identity comes from the JWT — the body is an empty JSON object.
        client.postRequest("/functions/v1/delete-account") { setBody(JsonObject(emptyMap())) }
}
