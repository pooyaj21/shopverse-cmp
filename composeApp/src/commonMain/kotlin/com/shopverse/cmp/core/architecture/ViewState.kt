package com.shopverse.cmp.core.architecture

sealed class ViewState {
    data class Loading(val onFrontOfContent: Boolean = true) : ViewState()
    data class Error(val message: String) : ViewState()
    data object Success : ViewState()
}
