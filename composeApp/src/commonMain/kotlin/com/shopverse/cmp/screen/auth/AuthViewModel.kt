package com.shopverse.cmp.screen.auth

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.core.architecture.BaseViewModelState
import com.shopverse.cmp.core.architecture.ViewState
import com.shopverse.cmp.network.service.util.AppResult
import com.shopverse.cmp.network.useCase.LoginUseCase
import com.shopverse.cmp.network.useCase.SignUpUseCase
import kotlinx.coroutines.launch

enum class AuthMode { Login, Register }

data class AuthModel(
    val mode: AuthMode,
    val isSubmitting: Boolean,
)

sealed interface AuthEffect {
    data object Completed : AuthEffect
    data class ShowError(val text: String) : AuthEffect
}

/**
 * The login / sign-up bottom sheet's ViewModel, ported from the Android app's
 * AuthBottomSheetViewModel: one sheet, a Login <-> Register mode toggle, client-side
 * validation, and Supabase error codes mapped to friendly messages.
 */
class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase,
) : BaseViewModelState<AuthModel, AuthEffect>(
    initialState = ViewState.Success,
    initialModel = AuthModel(mode = AuthMode.Login, isSubmitting = false),
) {

    private val currentModel: AuthModel
        get() = data ?: AuthModel(mode = AuthMode.Login, isSubmitting = false)

    /** Fresh sheet every time it opens — the Koin ViewModel outlives a dismissed sheet. */
    fun reset() {
        setSuccessState(AuthModel(mode = AuthMode.Login, isSubmitting = false))
    }

    fun switchMode() {
        if (currentModel.isSubmitting) return
        val next = when (currentModel.mode) {
            AuthMode.Login -> AuthMode.Register
            AuthMode.Register -> AuthMode.Login
        }
        setSuccessState(currentModel.copy(mode = next))
    }

    fun submit(name: String, email: String, password: String) {
        if (currentModel.isSubmitting) return
        val validationError = validate(currentModel.mode, name, email, password)
        if (validationError != null) {
            viewModelScope.launch { sendEffect(AuthEffect.ShowError(validationError)) }
            return
        }
        viewModelScope.launch {
            setSuccessState(currentModel.copy(isSubmitting = true))
            val result = when (currentModel.mode) {
                AuthMode.Login -> loginUseCase(email = email, password = password)
                AuthMode.Register -> signUpUseCase(name = name, email = email, password = password)
            }
            setSuccessState(currentModel.copy(isSubmitting = false))
            when (result) {
                is AppResult.Success -> sendEffect(AuthEffect.Completed)
                is AppResult.Error.Local ->
                    sendEffect(AuthEffect.ShowError("Network problem. Please try again."))
                is AppResult.Error.Remote ->
                    sendEffect(AuthEffect.ShowError(prettyRemoteError(result.httpCode, result.message)))
            }
        }
    }

    private fun validate(mode: AuthMode, name: String, email: String, password: String): String? =
        when {
            mode == AuthMode.Register && name.isBlank() -> "Please enter your name."
            email.isBlank() || !email.contains('@') -> "Please enter a valid email."
            password.length < MIN_PASSWORD_LENGTH ->
                "Password must be at least $MIN_PASSWORD_LENGTH characters."
            else -> null
        }

    private fun prettyRemoteError(httpCode: Int, message: String?): String {
        if (message.isNullOrBlank()) return "Something went wrong ($httpCode)."
        return when {
            message.contains("invalid_grant", ignoreCase = true) ||
                message.contains("invalid login credentials", ignoreCase = true) ->
                "Wrong email or password."
            message.contains("user_already_exists", ignoreCase = true) ||
                message.contains("already registered", ignoreCase = true) ->
                "An account with this email already exists."
            message.contains("weak_password", ignoreCase = true) ->
                "Password is too weak. Use at least $MIN_PASSWORD_LENGTH characters."
            message.contains("email_confirmation_required", ignoreCase = true) ->
                "Check your email to confirm your account, then log in."
            else -> "Something went wrong ($httpCode)."
        }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
