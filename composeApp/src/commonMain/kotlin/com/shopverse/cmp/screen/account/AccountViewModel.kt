package com.shopverse.cmp.screen.account

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModelState
import com.shopverse.cmp.network.service.util.AppResult
import com.shopverse.cmp.network.useCase.DeleteAccountUseCase
import com.shopverse.cmp.network.useCase.FetchProfileUseCase
import com.shopverse.cmp.network.useCase.GetSavedProfileUseCase
import kotlinx.coroutines.launch

data class AccountModel(
    val name: String?,
    val email: String?,
)

sealed interface AccountEffect {
    data object AccountDeleted : AccountEffect
    data class ShowMessage(val text: String) : AccountEffect
}

/**
 * Account screen ViewModel, ported from the Android app's AccountViewModel: shows the saved
 * profile immediately, silently refreshes it from `GET /auth/v1/user`, and handles the
 * delete-account edge function.
 */
class AccountViewModel(
    private val getSavedProfile: GetSavedProfileUseCase,
    private val fetchProfile: FetchProfileUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
) : BaseViewModelState<AccountModel, AccountEffect>() {

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val saved = getSavedProfile()
            if (saved == null) {
                // Unreachable via normal navigation — the Profile row only shows when logged in.
                setErrorState("You're not logged in.")
                return@launch
            }
            setSuccessState(AccountModel(name = saved.name, email = saved.email))

            // Refresh from the server; keep showing the saved profile if it fails.
            val result = fetchProfile()
            if (result is AppResult.Success) {
                setSuccessState(AccountModel(name = result.value.name, email = result.value.email))
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            setLoadingState(onFrontOfContent = true)
            when (val result = deleteAccountUseCase()) {
                is AppResult.Success -> sendEffect(AccountEffect.AccountDeleted)
                is AppResult.Error.Local -> {
                    refresh()
                    sendEffect(AccountEffect.ShowMessage("Network problem. Please try again."))
                }
                is AppResult.Error.Remote -> {
                    refresh()
                    sendEffect(
                        AccountEffect.ShowMessage(
                            if (result.httpCode == 401) {
                                "Please log in again to delete your account."
                            } else {
                                "Couldn't delete your account (${result.httpCode})."
                            },
                        ),
                    )
                }
            }
        }
    }
}
