package com.shopverse.cmp

import android.content.Intent
import android.net.Uri
import com.shopverse.cmp.ShopVerseApplication.Companion.appContext
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual val platformName: String get() = "Android"

actual val versionName: String get() = BuildConfig.VERSION_NAME

actual val isDebug: Boolean get() = BuildConfig.DEBUG

actual val requestEngin: HttpClientEngine get() = OkHttp.create()

actual fun openLink(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    appContext.startActivity(intent)
}
