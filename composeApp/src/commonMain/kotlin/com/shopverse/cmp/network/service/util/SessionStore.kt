package com.shopverse.cmp.network.service.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.shopverse.cmp.core.dataStore.Keys
import com.shopverse.cmp.core.dataStore.getObject
import com.shopverse.cmp.core.dataStore.getString
import com.shopverse.cmp.core.dataStore.saveObject
import com.shopverse.cmp.core.dataStore.saveString
import com.shopverse.cmp.model.UserProfile
import com.shopverse.cmp.network.model.response.AuthResponse

/**
 * Persists the Supabase session (access + refresh tokens + user id) in DataStore.
 *
 * Supabase **rotates the refresh token on every use**, so [save] must be called after every
 * login AND every silent refresh, overwriting the old refresh token — otherwise the next
 * refresh uses a spent token and the session dies.
 */
object SessionStore {

    fun save(prefs: DataStore<Preferences>, auth: AuthResponse) {
        auth.accessToken?.let { prefs.saveString(Keys.ACCESS_TOKEN, it) }
        auth.refreshToken?.let { prefs.saveString(Keys.REFRESH_TOKEN, it) }
        auth.user?.id?.let { prefs.saveString(Keys.USER_ID, it) }
    }

    fun clear(prefs: DataStore<Preferences>) {
        prefs.saveString(Keys.ACCESS_TOKEN, null)
        prefs.saveString(Keys.REFRESH_TOKEN, null)
        prefs.saveString(Keys.USER_ID, null)
        prefs.saveString(Keys.PROFILE, null)
    }

    /** Saved separately from [save] — name/email come from the caller (fallback name on signup). */
    fun saveProfile(prefs: DataStore<Preferences>, profile: UserProfile) {
        prefs.saveObject(Keys.PROFILE, profile, UserProfile.serializer())
    }

    fun profile(prefs: DataStore<Preferences>): UserProfile? =
        prefs.getObject(Keys.PROFILE, UserProfile.serializer())

    fun accessToken(prefs: DataStore<Preferences>): String? = prefs.getString(Keys.ACCESS_TOKEN)
    fun refreshToken(prefs: DataStore<Preferences>): String? = prefs.getString(Keys.REFRESH_TOKEN)
    fun userId(prefs: DataStore<Preferences>): String? = prefs.getString(Keys.USER_ID)
    fun isLoggedIn(prefs: DataStore<Preferences>): Boolean = accessToken(prefs) != null
}
