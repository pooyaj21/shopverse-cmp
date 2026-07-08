package com.shopverse.cmp.network.service.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shopverse.cmp.core.architecture.util.tryOptional
import com.shopverse.cmp.network.model.request.RefreshTokenRequest
import com.shopverse.cmp.network.service.service.AuthServiceImpl
import com.shopverse.cmp.requestEngin
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Silent refresh-and-retry, mirroring ProvinCompose's device-auth refresh but against Supabase.
 *
 * On a 401 (expired access token) we exchange the stored refresh token for a fresh session via
 * `grant_type=refresh_token`, persist the ROTATED tokens, and throw [TokenRefreshedException] so
 * [safeApiCall] replays the original request. A [Mutex] serializes refreshes — concurrent 401s
 * would otherwise each spend the refresh token and invalidate the session (rotation hazard).
 */
fun HttpClientConfig<*>.authConfig(
    prefs: DataStore<Preferences>,
) {
    val mutex = Mutex()

    HttpResponseValidator {
        validateResponse { response ->
            if (response.status != HttpStatusCode.Unauthorized) return@validateResponse

            val didRefresh = runBlocking {
                mutex.withLock {
                    val refreshToken = SessionStore.refreshToken(prefs) ?: return@withLock false
                    tryOptional {
                        val authApi = AuthServiceImpl(createRefreshClient(prefs))
                        val refreshed = authApi.refresh(RefreshTokenRequest(refreshToken))
                        if (refreshed.accessToken != null) {
                            SessionStore.save(prefs, refreshed)
                            true
                        } else {
                            false
                        }
                    } ?: false
                }
            }

            if (didRefresh) throw TokenRefreshedException()
        }
    }
}

/** Bare client used only to hit the refresh endpoint — no auth validator, so no recursion. */
private fun createRefreshClient(
    prefs: DataStore<Preferences>,
) = HttpClient(requestEngin) {
    expectSuccess = true
    defaultConfig(prefs)
}
