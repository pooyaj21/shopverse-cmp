package com.shopverse.cmp.network.useCase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shopverse.cmp.core.dataStore.Keys
import com.shopverse.cmp.core.dataStore.getString
import com.shopverse.cmp.core.dataStore.saveString

/**
 * First-run onboarding flag, persisted in DataStore under [Keys.ONBOARDING_DONE].
 *
 * The Android app models this as a 3-state `AppStage` enum; here a single boolean flag is enough
 * because the CMP flow is splash -> onboarding (once) -> home, with auth layered on top on demand.
 */

private const val FLAG_TRUE = "true"

interface IsOnboardingDoneUseCase : UseCase {
    operator fun invoke(): Boolean
}

class IsOnboardingDoneUseCaseImpl(
    private val prefs: DataStore<Preferences>,
) : IsOnboardingDoneUseCase {
    override fun invoke(): Boolean = prefs.getString(Keys.ONBOARDING_DONE) == FLAG_TRUE
}

interface CompleteOnboardingUseCase : UseCase {
    operator fun invoke()
}

class CompleteOnboardingUseCaseImpl(
    private val prefs: DataStore<Preferences>,
) : CompleteOnboardingUseCase {
    override fun invoke() = prefs.saveString(Keys.ONBOARDING_DONE, FLAG_TRUE)
}
