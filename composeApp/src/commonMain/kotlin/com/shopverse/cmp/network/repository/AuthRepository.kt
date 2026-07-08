package com.shopverse.cmp.network.repository

import com.shopverse.cmp.network.model.request.LoginRequest
import com.shopverse.cmp.network.model.request.SignUpRequest
import com.shopverse.cmp.network.model.response.AuthResponse
import com.shopverse.cmp.network.service.service.AuthService
import com.shopverse.cmp.network.service.util.AppResult
import com.shopverse.cmp.network.service.util.safeApiCall

interface AuthRepository {
    suspend fun signUp(name: String, email: String, password: String): AppResult<AuthResponse>
    suspend fun login(email: String, password: String): AppResult<AuthResponse>
    suspend fun logout(): AppResult<Unit>
}

class AuthRepositoryImpl(
    private val authService: AuthService,
) : AuthRepository {

    override suspend fun signUp(name: String, email: String, password: String): AppResult<AuthResponse> =
        safeApiCall {
            authService.signUp(
                SignUpRequest(
                    email = email,
                    password = password,
                    data = SignUpRequest.SignUpData(name = name),
                ),
            )
        }

    override suspend fun login(email: String, password: String): AppResult<AuthResponse> =
        safeApiCall { authService.login(LoginRequest(email = email, password = password)) }

    override suspend fun logout(): AppResult<Unit> =
        safeApiCall { authService.logout() }
}
