package com.shopverse.cmp.core.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.shopverse.cmp.core.architecture.util.tryOptional
import com.shopverse.cmp.network.service.util.jsonFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer

/**
 * Blocking accessors over DataStore — matching ProvinCompose. They are blocking (not suspend)
 * so the Ktor `defaultRequest { }` block can read the access token per request. Reads/writes
 * are cheap and off the hot path, so the blocking bridge is acceptable here.
 */

fun DataStore<Preferences>.getString(key: String): String? = runBlocking {
    data.first()[stringPreferencesKey(key)]
}

fun DataStore<Preferences>.saveString(key: String, value: String?) {
    runBlocking {
        edit { prefs ->
            val k = stringPreferencesKey(key)
            if (value == null) prefs.remove(k) else prefs[k] = value
        }
    }
}

fun <T> DataStore<Preferences>.getObject(key: String, serializer: KSerializer<T>): T? {
    val raw = getString(key) ?: return null
    return tryOptional { jsonFormatter.decodeFromString(serializer, raw) }
}

fun <T> DataStore<Preferences>.saveObject(key: String, value: T?, serializer: KSerializer<T>) {
    if (value == null) saveString(key, null)
    else saveString(key, jsonFormatter.encodeToString(serializer, value))
}
