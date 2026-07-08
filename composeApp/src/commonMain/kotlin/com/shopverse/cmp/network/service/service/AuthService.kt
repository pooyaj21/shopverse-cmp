package com.shopverse.cmp.network.service.service

import com.shopverse.cmp.network.model.request.LoginRequest
import com.shopverse.cmp.network.model.request.RefreshTokenRequest
import com.shopverse.cmp.network.model.request.SignUpRequest
import com.shopverse.cmp.network.model.response.AuthResponse
import com.shopverse.cmp.network.service.util.postRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody

interface AuthService {
    suspend fun signUp(request: SignUpRequest): AuthResponse
    suspend fun login(request: LoginRequest): AuthResponse
    suspend fun refresh(request: RefreshTokenRequest): AuthResponse
    suspend fun logout()
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
}
