package com.shopverse.cmp.network.service

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized

/**
 * Global network config, initialized once at app start (see App.kt). Mirrors ProvinCompose's
 * `EnvironmentConfiguration` but trimmed to what Supabase needs: base URL + anon key + debug flag.
 */
class EnvironmentConfiguration private constructor(
    val baseUrl: String,
    val anonKey: String,
    val applicationName: String,
    val appVersionName: String,
    val isInDebugMode: Boolean,
) {
    @OptIn(InternalCoroutinesApi::class)
    companion object : SynchronizedObject() {

        private var instance: EnvironmentConfiguration? = null

        fun init(
            baseUrl: String,
            anonKey: String,
            applicationName: String,
            appVersionName: String,
            isInDebugMode: Boolean,
        ) = synchronized(this) {
            check(instance == null) { "EnvironmentConfiguration is already initialized!" }
            instance = EnvironmentConfiguration(
                baseUrl = baseUrl.trimEnd('/'),
                anonKey = anonKey,
                applicationName = applicationName,
                appVersionName = appVersionName,
                isInDebugMode = isInDebugMode,
            )
        }

        fun getInstance(): EnvironmentConfiguration =
            checkNotNull(instance) { "EnvironmentConfiguration is not initialized!" }

        val isInitialized: Boolean get() = instance != null
    }
}
