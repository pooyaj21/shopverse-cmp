package com.shopverse.cmp.core.dataStore

import androidx.compose.runtime.Composable

/** Per-platform absolute path to the DataStore file (filesDir on Android, Documents on iOS). */
@Composable
expect fun getProducePath(): String
