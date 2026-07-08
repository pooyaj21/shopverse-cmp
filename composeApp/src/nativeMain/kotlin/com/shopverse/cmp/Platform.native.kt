package com.shopverse.cmp

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import platform.Foundation.NSBundle
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import kotlin.experimental.ExperimentalNativeApi

actual val platformName: String get() = "iOS"

actual val versionName: String
    get() = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String
        ?: "1.0.0"

@OptIn(ExperimentalNativeApi::class)
actual val isDebug: Boolean get() = Platform.isDebugBinary

actual val requestEngin: HttpClientEngine get() = Darwin.create()

actual fun openLink(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}
