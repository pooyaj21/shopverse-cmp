package com.shopverse.cmp.network.useCase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.shopverse.cmp.core.dataStore.Keys
import com.shopverse.cmp.core.dataStore.getString
import com.shopverse.cmp.core.dataStore.saveString
import com.shopverse.cmp.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Theme preference, persisted in DataStore under [Keys.THEME_MODE]. Defaults to SYSTEM. */

private fun String?.toThemeMode(): ThemeMode =
    ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.SYSTEM

interface GetThemeModeUseCase : UseCase {
    operator fun invoke(): ThemeMode
}

class GetThemeModeUseCaseImpl(
    private val prefs: DataStore<Preferences>,
) : GetThemeModeUseCase {
    override fun invoke(): ThemeMode = prefs.getString(Keys.THEME_MODE).toThemeMode()
}

interface SetThemeModeUseCase : UseCase {
    operator fun invoke(mode: ThemeMode)
}

class SetThemeModeUseCaseImpl(
    private val prefs: DataStore<Preferences>,
) : SetThemeModeUseCase {
    override fun invoke(mode: ThemeMode) = prefs.saveString(Keys.THEME_MODE, mode.name)
}

interface ObserveThemeModeUseCase : UseCase {
    operator fun invoke(): Flow<ThemeMode>
}

class ObserveThemeModeUseCaseImpl(
    private val prefs: DataStore<Preferences>,
) : ObserveThemeModeUseCase {
    override fun invoke(): Flow<ThemeMode> = prefs.data
        .map { it[stringPreferencesKey(Keys.THEME_MODE)].toThemeMode() }
        .distinctUntilChanged()
}
