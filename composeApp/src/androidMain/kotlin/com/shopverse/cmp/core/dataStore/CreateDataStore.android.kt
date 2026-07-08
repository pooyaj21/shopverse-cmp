package com.shopverse.cmp.core.dataStore

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getProducePath(): String {
    val context = LocalContext.current
    return context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath
}
