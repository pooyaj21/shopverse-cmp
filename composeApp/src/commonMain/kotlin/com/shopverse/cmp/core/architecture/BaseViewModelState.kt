package com.shopverse.cmp.core.architecture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base for ViewModels that expose a UI [ViewState] plus a data [Model]. The pattern (and the
 * setLoading/setSuccess/setError helpers) is ported directly from ProvinCompose.
 */
abstract class BaseViewModelState<Model, ViewEffect>(
    initialState: ViewState = ViewState.Loading(),
    initialModel: Model? = null,
) : BaseViewModel<ViewEffect>() {

    private val mutableUiStateFlow = MutableStateFlow(initialState)
    val uiStateFlow = mutableUiStateFlow.asStateFlow()
    val state: ViewState get() = mutableUiStateFlow.value

    private val mutableDataFlow = MutableStateFlow(initialModel)
    val dataFlow = mutableDataFlow.asStateFlow()
    val data: Model? get() = mutableDataFlow.value

    protected fun setLoadingState(onFrontOfContent: Boolean = true) {
        mutableUiStateFlow.value = ViewState.Loading(onFrontOfContent)
    }

    protected fun setSuccessState(model: Model) {
        mutableDataFlow.value = model
        mutableUiStateFlow.value = ViewState.Success
    }

    protected fun setErrorState(message: String) {
        mutableUiStateFlow.value = ViewState.Error(message)
    }
}
