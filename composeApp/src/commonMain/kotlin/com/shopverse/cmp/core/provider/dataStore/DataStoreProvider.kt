package com.shopverse.cmp.core.provider.dataStore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.shopverse.cmp.core.dataStore.getProducePath
import okio.Path.Companion.toPath

val localDataStore = staticCompositionLocalOf<DataStore<Preferences>> {
    error("No DataStore provided")
}

/** Holds the singleton so non-composable code (DI, network layer) can reach it. */
object DataStoreProvider {
    var dataStore: DataStore<Preferences>? = null
}

@Composable
fun DataStoreProvider(content: @Composable () -> Unit) {
    val path = getProducePath().toPath()
    if (DataStoreProvider.dataStore == null) {
        DataStoreProvider.dataStore =
            PreferenceDataStoreFactory.createWithPath(produceFile = { path })
    }
    CompositionLocalProvider(localDataStore provides DataStoreProvider.dataStore!!) {
        content()
    }
}
