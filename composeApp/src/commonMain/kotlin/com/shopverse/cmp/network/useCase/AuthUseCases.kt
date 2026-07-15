package com.shopverse.cmp.network.useCase

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shopverse.cmp.model.UserProfile
import com.shopverse.cmp.network.model.response.AuthResponse
import com.shopverse.cmp.network.repository.AuthRepository
import com.shopverse.cmp.network.service.util.AppResult
import com.shopverse.cmp.network.service.util.SessionStore
import com.shopverse.cmp.network.service.util.doOnSuccess
import com.shopverse.cmp.network.service.util.mapIfSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stores tokens + profile from a successful auth response, mirroring Android's
 * AuthRepositoryImpl.persistAndMap. A 2xx response without a session (signup while email
 * confirmation is enabled) becomes a remote error so the sheet can tell the user to go confirm.
 */
private fun persistSession(
    prefs: DataStore<Preferences>,
    auth: AuthResponse,
    fallbackName: String? = null,
): AppResult<UserProfile> {
    val user = auth.user
    if (auth.accessToken.isNullOrBlank() || user == null) {
        return AppResult.Error.Remote(
            httpCode = 200,
            message = "email_confirmation_required",
            cause = null,
        )
    }
    SessionStore.save(prefs, auth)
    val profile = UserProfile(
        id = user.id,
        name = user.userMetadata?.name ?: fallbackName,
        email = user.email,
    )
    SessionStore.saveProfile(prefs, profile)
    return AppResult.Success(profile)
}

interface LoginUseCase : UseCase {
    suspend operator fun invoke(email: String, password: String): AppResult<UserProfile>
}

class LoginUseCaseImpl(
    private val authRepository: AuthRepository,
    private val prefs: DataStore<Preferences>,
) : LoginUseCase {
    override suspend fun invoke(email: String, password: String): AppResult<UserProfile> =
        withContext(Dispatchers.Default) {
            when (val result = authRepository.login(email, password)) {
                is AppResult.Success -> persistSession(prefs, result.value)
                is AppResult.Error -> result
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
            when (val result = authRepository.signUp(name, email, password)) {
                is AppResult.Success -> persistSession(prefs, result.value, fallbackName = name)
                is AppResult.Error -> result
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

interface GetSavedProfileUseCase : UseCase {
    operator fun invoke(): UserProfile?
}

class GetSavedProfileUseCaseImpl(
    private val prefs: DataStore<Preferences>,
) : GetSavedProfileUseCase {
    override fun invoke(): UserProfile? =
        if (SessionStore.isLoggedIn(prefs)) SessionStore.profile(prefs) else null
}

interface FetchProfileUseCase : UseCase {
    suspend operator fun invoke(): AppResult<UserProfile>
}

/** `GET /auth/v1/user` — refreshes the locally saved profile from the server. */
class FetchProfileUseCaseImpl(
    private val authRepository: AuthRepository,
    private val prefs: DataStore<Preferences>,
) : FetchProfileUseCase {
    override suspend fun invoke(): AppResult<UserProfile> = withContext(Dispatchers.Default) {
        authRepository.fetchUser().mapIfSuccess { user ->
            val profile = UserProfile(
                id = user.id,
                name = user.userMetadata?.name,
                email = user.email,
            )
            SessionStore.saveProfile(prefs, profile)
            profile
        }
    }
}

interface DeleteAccountUseCase : UseCase {
    suspend operator fun invoke(): AppResult<Unit>
}

/** `POST /functions/v1/delete-account` — the session is gone server-side, so clear it locally. */
class DeleteAccountUseCaseImpl(
    private val authRepository: AuthRepository,
    private val prefs: DataStore<Preferences>,
) : DeleteAccountUseCase {
    override suspend fun invoke(): AppResult<Unit> = withContext(Dispatchers.Default) {
        authRepository.deleteAccount().doOnSuccess { SessionStore.clear(prefs) }
    }
}
