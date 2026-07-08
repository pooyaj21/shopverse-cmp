package com.shopverse.cmp.screen.onboarding

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModel
import com.shopverse.cmp.network.useCase.CompleteOnboardingUseCase
import kotlinx.coroutines.launch

sealed interface OnboardingEffect {
    /** User finished onboarding; move on to home and don't show onboarding again. */
    data object Continue : OnboardingEffect
}

class OnboardingViewModel(
    private val completeOnboarding: CompleteOnboardingUseCase,
) : BaseViewModel<OnboardingEffect>() {

    fun onContinueClick() {
        viewModelScope.launch {
            completeOnboarding()
            sendEffect(OnboardingEffect.Continue)
        }
    }
}
