package com.shopverse.cmp.core.architecture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun <ViewEffect> SharedFlow<ViewEffect>.onEffect(
    action: (ViewEffect) -> Unit,
) {
    LaunchedEffect(this) {
        collect { effect -> action(effect) }
    }
}
