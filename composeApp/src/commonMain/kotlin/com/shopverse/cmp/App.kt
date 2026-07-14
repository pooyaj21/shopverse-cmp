package com.shopverse.cmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.shopverse.cmp.core.provider.database.DatabaseProvider
import com.shopverse.cmp.core.provider.dataStore.DataStoreProvider
import com.shopverse.cmp.core.theme.ShopVerseTheme
import com.shopverse.cmp.model.ThemeMode
import com.shopverse.cmp.network.service.EnvironmentConfiguration
import com.shopverse.cmp.network.service.SupabaseSecrets
import com.shopverse.cmp.network.useCase.ObserveThemeModeUseCase
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
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
            val themeMode by koinInject<ObserveThemeModeUseCase>().invoke()
                .collectAsState(initial = ThemeMode.SYSTEM)
            ShopVerseTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                NavigationStack(navController = navController)
            }
        }
    }
}
