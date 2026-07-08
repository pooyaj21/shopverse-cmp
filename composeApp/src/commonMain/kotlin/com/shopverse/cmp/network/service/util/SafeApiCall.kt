package com.shopverse.cmp.network.service.util

import com.shopverse.cmp.core.architecture.util.tryOptional
import com.shopverse.cmp.network.model.base.ErrorResponse
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText

/**
 * Wraps a Ktor call, funnelling every outcome into [AppResult]. A [TokenRefreshedException]
 * (thrown by the auth plugin after a successful silent refresh) transparently retries the call.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> T): AppResult<T> {
    return try {
        AppResult.Success(apiCall())
    } catch (_: TokenRefreshedException) {
        safeApiCall(apiCall)
    } catch (e: ResponseException) {
        val parsed: ErrorResponse? = tryOptional {
            jsonFormatter.decodeFromString<ErrorResponse>(e.response.bodyAsText())
        }
        AppResult.Error.Remote(
            httpCode = e.response.status.value,
            message = parsed?.bestMessage,
            cause = e,
        )
    } catch (e: Exception) {
        AppResult.Error.Local(message = e.message, cause = e)
    }
}
