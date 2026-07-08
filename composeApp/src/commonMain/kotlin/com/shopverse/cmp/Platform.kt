package com.shopverse.cmp

import io.ktor.client.engine.HttpClientEngine

expect val platformName: String

expect val versionName: String

expect val isDebug: Boolean

/** Per-platform Ktor engine: OkHttp on Android, Darwin on iOS. */
expect val requestEngin: HttpClientEngine

/** Opens an external URL (e.g. a scanned QR deeplink) in the platform browser/handler. */
expect fun openLink(url: String)
