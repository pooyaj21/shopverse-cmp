package com.shopverse.cmp.screen.splash

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModel
import com.shopverse.cmp.network.useCase.IsOnboardingDoneUseCase
import com.shopverse.cmp.screen.Screen
import kotlinx.coroutines.launch

sealed interface SplashEffect {
    /** Where to go once the splash animation finishes; the splash is popped off the back stack. */
    data class Navigate(val destination: Screen) : SplashEffect
}

/**
 * Decides the post-splash destination. Mirrors the Android SplashViewModel, which reads the app
 * stage on animation-end: first-run -> onboarding, otherwise straight to home.
 */
class SplashViewModel(
    private val isOnboardingDone: IsOnboardingDoneUseCase,
) : BaseViewModel<SplashEffect>() {

    private var routed = false

    /** Invoked when the Lottie animation completes (or the safety timeout fires). Idempotent. */
    fun onSplashFinished() {
        if (routed) return
        routed = true
        viewModelScope.launch {
            val destination = if (isOnboardingDone()) Screen.Main else Screen.Onboarding
            sendEffect(SplashEffect.Navigate(destination))
        }
    }
}
