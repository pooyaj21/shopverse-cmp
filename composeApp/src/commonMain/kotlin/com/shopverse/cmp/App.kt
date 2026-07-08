package com.shopverse.cmp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.shopverse.cmp.core.provider.database.DatabaseProvider
import com.shopverse.cmp.core.provider.dataStore.DataStoreProvider
import com.shopverse.cmp.core.theme.ShopVerseTheme
import com.shopverse.cmp.network.service.EnvironmentConfiguration
import com.shopverse.cmp.network.service.SupabaseSecrets
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.core.context.startKoin

private var koinStarted = false

@Composable
@Preview
fun App() {
    if (!EnvironmentConfiguration.isInitialized) {
        EnvironmentConfiguration.init(
            baseUrl = SupabaseSecrets.URL,
            anonKey = SupabaseSecrets.ANON_KEY,
            applicationName = "ShopVerse",
            appVersionName = versionName,
            isInDebugMode = isDebug,
        )
    }

    // Platform providers must run BEFORE startKoin so the DataStore/DAO singletons exist when
    // the Koin module resolves them (mirrors ProvinCompose's provider-then-Koin ordering).
    DataStoreProvider {
        DatabaseProvider {
            if (!koinStarted) {
                startKoin { modules(appKoinModule) }
                koinStarted = true
            }
            ShopVerseTheme {
                val navController = rememberNavController()
                NavigationStack(navController = navController)
            }
        }
    }
}
