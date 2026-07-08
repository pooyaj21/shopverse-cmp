package com.shopverse.cmp.core.architecture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Renders [Model] once loaded, showing a spinner while loading and an [ErrorView] on failure.
 * Keeps the last successful data visible under an overlaid spinner during refreshes — same
 * behaviour as ProvinCompose's `Render`, minus its color-provider / pull-to-refresh coupling.
 */
@Composable
fun <Model> BaseViewModelState<Model, *>.Render(
    onRetry: () -> Unit = {},
    content: @Composable BoxScope.(Model) -> Unit,
) {
    val uiState by uiStateFlow.collectAsState()
    val data by dataFlow.collectAsState()
    var lastData by remember { mutableStateOf<Model?>(null) }
    if (data != null) lastData = data

    Box(modifier = Modifier.fillMaxSize()) {
        lastData?.let { content(it) }

        when (val s = uiState) {
            is ViewState.Loading ->
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            is ViewState.Error ->
                if (lastData == null) ErrorView(message = s.message, onRetry = onRetry)
            ViewState.Success -> Unit
        }
    }
}
