package com.shopverse.cmp.network.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shopverse.cmp.network.service.util.authConfig
import com.shopverse.cmp.network.service.util.defaultConfig
import com.shopverse.cmp.requestEngin
import io.ktor.client.HttpClient

/**
 * The single shared Ktor client. `expectSuccess = true` makes non-2xx responses throw a
 * ResponseException (caught by safeApiCall) and lets the auth validator fire on 401.
 */
fun createHttpClient(
    prefs: DataStore<Preferences>,
) = HttpClient(requestEngin) {
    expectSuccess = true
    defaultConfig(prefs)
    authConfig(prefs)
}
