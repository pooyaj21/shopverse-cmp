package com.shopverse.cmp.core.architecture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Screen scaffold wrapper. Overloads mirror ProvinCompose's `Route`: a bare version, a
 * ViewModel-aware version that pumps one-shot effects, and a state-aware version that renders
 * loading/error/content for you.
 */
@Composable
fun Route(
    topBar: @Composable () -> Unit = {},
    screen: @Composable BoxScope.() -> Unit,
) {
    Scaffold(topBar = topBar) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    PaddingValues(
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding(),
                    ),
                ),
        ) {
            screen()
        }
    }
}

@Composable
fun <ViewEffect> Route(
    viewModel: BaseViewModel<ViewEffect>,
    topBar: @Composable () -> Unit = {},
    onEffect: ((ViewEffect) -> Unit)? = null,
    screen: @Composable BoxScope.() -> Unit,
) {
    onEffect?.let { viewModel.effectFlow.onEffect(it) }
    Route(topBar = topBar, screen = screen)
}

@Composable
fun <Model, ViewEffect> Route(
    viewModel: BaseViewModelState<Model, ViewEffect>,
    topBar: @Composable () -> Unit = {},
    onRetry: () -> Unit = {},
    onEffect: ((ViewEffect) -> Unit)? = null,
    screen: @Composable BoxScope.(model: Model) -> Unit,
) {
    onEffect?.let { viewModel.effectFlow.onEffect(it) }
    Route(topBar = topBar) {
        viewModel.Render(onRetry = onRetry) { model -> screen(model) }
    }
}
