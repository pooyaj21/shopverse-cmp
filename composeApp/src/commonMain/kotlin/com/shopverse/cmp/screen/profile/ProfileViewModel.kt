package com.shopverse.cmp.screen.profile

import androidx.lifecycle.viewModelScope
import com.shopverse.cmp.buildNumber
import com.shopverse.cmp.core.architecture.BaseViewModelState
import com.shopverse.cmp.model.ThemeMode
import com.shopverse.cmp.model.label
import com.shopverse.cmp.network.useCase.GetThemeModeUseCase
import com.shopverse.cmp.network.useCase.IsLoggedInUseCase
import com.shopverse.cmp.network.useCase.LogoutUseCase
import com.shopverse.cmp.network.useCase.SetThemeModeUseCase
import com.shopverse.cmp.versionName
import kotlinx.coroutines.launch

/** The profile screen's row list, ported from the Android app's ProfileUiModel. */
data class ProfileModel(val items: List<ProfileItem>)

sealed interface ProfileItem {

    data class Separator(val isLast: Boolean = false) : ProfileItem

    data class Title(val title: String) : ProfileItem

    sealed class Navigatable(val title: String) : ProfileItem {
        data object Account : Navigatable("Profile")
        data object Orders : Navigatable("Orders")
    }

    sealed class Simple(val title: String) : ProfileItem {
        data object Login : Simple("Log in / Create account")
        data object Logout : Simple("Log out")
    }

    data class Theme(val mode: ThemeMode) : ProfileItem {
        val title: String = "Theme"
        val value: String = mode.label
    }

    sealed class Info(val title: String, val value: String) : ProfileItem {
        class AppVersion(value: String) : Info("App version", value)
        class AppBuildNumber(value: String) : Info("Build number", value)
    }
}

sealed interface ProfileEffect {
    data class ShowMessage(val text: String) : ProfileEffect
    data object ShowLogin : ProfileEffect
    data object OpenAccount : ProfileEffect
    data object OpenOrders : ProfileEffect
}

class ProfileViewModel(
    private val isLoggedIn: IsLoggedInUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getThemeMode: GetThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase,
) : BaseViewModelState<ProfileModel, ProfileEffect>() {

    init {
        refresh()
    }

    fun refresh() {
        setSuccessState(createModel(isLoggedIn = isLoggedIn(), themeMode = getThemeMode()))
    }

    fun setTheme(mode: ThemeMode) {
        setThemeMode.invoke(mode)
        refresh()
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            refresh()
        }
    }

    fun onItemClick(item: ProfileItem) {
        viewModelScope.launch {
            when (item) {
                ProfileItem.Simple.Login -> sendEffect(ProfileEffect.ShowLogin)
                ProfileItem.Simple.Logout -> logout()
                ProfileItem.Navigatable.Account -> sendEffect(ProfileEffect.OpenAccount)
                ProfileItem.Navigatable.Orders -> sendEffect(ProfileEffect.OpenOrders)
                else -> Unit
            }
        }
    }

    private fun createModel(isLoggedIn: Boolean, themeMode: ThemeMode): ProfileModel {
        val items = buildList {
            add(ProfileItem.Title("Account"))
            if (isLoggedIn) {
                add(ProfileItem.Navigatable.Account)
                add(ProfileItem.Separator())
                add(ProfileItem.Navigatable.Orders)
            } else {
                add(ProfileItem.Simple.Login)
            }

            add(ProfileItem.Title("App"))
            add(ProfileItem.Theme(themeMode))
            add(ProfileItem.Separator())
            add(ProfileItem.Info.AppVersion(versionName))
            add(ProfileItem.Separator())
            add(ProfileItem.Info.AppBuildNumber(buildNumber))

            if (isLoggedIn) {
                add(ProfileItem.Title(""))
                add(ProfileItem.Simple.Logout)
                add(ProfileItem.Separator(isLast = true))
            } else {
                add(ProfileItem.Separator(isLast = true))
            }
        }
        return ProfileModel(items)
    }
}
