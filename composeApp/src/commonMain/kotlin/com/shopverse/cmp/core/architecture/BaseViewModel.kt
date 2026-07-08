package com.shopverse.cmp.core.architecture

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Base for all screen ViewModels. Carries a one-shot [ViewEffect] channel (navigation, toasts).
 * Mirrors ProvinCompose's BaseViewModel, minus its app-specific Alert manager.
 */
abstract class BaseViewModel<ViewEffect> : ViewModel() {

    private val mutableEffectFlow by lazy { MutableSharedFlow<ViewEffect>() }
    val effectFlow by lazy { mutableEffectFlow.asSharedFlow() }

    protected suspend fun sendEffect(effect: ViewEffect) = mutableEffectFlow.emit(effect)
}
