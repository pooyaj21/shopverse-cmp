package com.shopverse.cmp.network.useCase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shopverse.cmp.model.UserProfile
import com.shopverse.cmp.network.model.response.AuthResponse
import com.shopverse.cmp.network.repository.AuthRepository
import com.shopverse.cmp.network.service.util.AppResult
import com.shopverse.cmp.network.service.util.SessionStore
import com.shopverse.cmp.network.service.util.mapIfSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun AuthResponse.toProfile(fallbackName: String? = null): UserProfile = UserProfile(
    id = user?.id.orEmpty(),
    name = user?.userMetadata?.name ?: fallbackName,
    email = user?.email,
)

interface LoginUseCase : UseCase {
    suspend operator fun invoke(email: String, password: String): AppResult<UserProfile>
}

class LoginUseCaseImpl(
    private val authRepository: AuthRepository,
    private val prefs: DataStore<Preferences>,
) : LoginUseCase {
    override suspend fun invoke(email: String, password: String): AppResult<UserProfile> =
        withContext(Dispatchers.Default) {
            authRepository.login(email, password).mapIfSuccess { auth ->
                SessionStore.save(prefs, auth)
                auth.toProfile()
            }
        }
}

interface SignUpUseCase : UseCase {
    suspend operator fun invoke(name: String, email: String, password: String): AppResult<UserProfile>
}

class SignUpUseCaseImpl(
    private val authRepository: AuthRepository,
    private val prefs: DataStore<Preferences>,
) : SignUpUseCase {
    override suspend fun invoke(name: String, email: String, password: String): AppResult<UserProfile> =
        withContext(Dispatchers.Default) {
            authRepository.signUp(name, email, password).mapIfSuccess { auth ->
                // When email confirmation is disabled the signup response already carries a session.
                SessionStore.save(prefs, auth)
                auth.toProfile(fallbackName = name)
            }
        }
}

interface LogoutUseCase : UseCase {
    suspend operator fun invoke(): AppResult<Unit>
}

class LogoutUseCaseImpl(
    private val authRepository: AuthRepository,
    private val prefs: DataStore<Preferences>,
) : LogoutUseCase {
    override suspend fun invoke(): AppResult<Unit> = withContext(Dispatchers.Default) {
        val result = authRepository.logout()
        // Drop local tokens regardless — the access token is valid up to 1h but we're done with it.
        SessionStore.clear(prefs)
        result.mapIfSuccess { }
    }
}

interface IsLoggedInUseCase : UseCase {
    operator fun invoke(): Boolean
}

class IsLoggedInUseCaseImpl(
    private val prefs: DataStore<Preferences>,
) : IsLoggedInUseCase {
    override fun invoke(): Boolean = SessionStore.isLoggedIn(prefs)
}
