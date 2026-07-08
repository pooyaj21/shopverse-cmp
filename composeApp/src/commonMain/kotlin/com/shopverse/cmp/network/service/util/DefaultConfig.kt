package com.shopverse.cmp.network.service.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shopverse.cmp.core.dataStore.Keys
import com.shopverse.cmp.core.dataStore.getString
import com.shopverse.cmp.network.service.EnvironmentConfiguration
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json

val BASE_URL: String get() = EnvironmentConfiguration.getInstance().baseUrl

/**
 * Headers + serialization every Supabase request shares. Supabase needs `apikey` on every
 * call and an `Authorization: Bearer <jwt>` — the user JWT when logged in, otherwise the anon
 * key (public catalog + public order detail rely on this). Mirrors ProvinCompose's defaultConfig.
 */
fun HttpClientConfig<*>.defaultConfig(
    prefs: DataStore<Preferences>,
) {
    val env = EnvironmentConfiguration.getInstance()

    install(Logging) {
        level = if (env.isInDebugMode) LogLevel.ALL else LogLevel.NONE
    }
    install(ContentNegotiation) {
        json(jsonFormatter)
    }

    defaultRequest {
        contentType(ContentType.Application.Json)
        header("apikey", env.anonKey)
        val token = prefs.getString(Keys.ACCESS_TOKEN)
        header("Authorization", "Bearer ${token ?: env.anonKey}")
        header(
            "User-Agent",
            "${env.applicationName}/${env.appVersionName} (${if (env.isInDebugMode) "debug" else "release"})",
        )
    }
}
