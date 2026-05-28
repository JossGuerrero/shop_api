package com.shopapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ACCESS_TOKEN  = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID       = intPreferencesKey("user_id")
        private val USERNAME      = stringPreferencesKey("username")
        private val USER_EMAIL    = stringPreferencesKey("user_email")
        private val IS_STAFF      = booleanPreferencesKey("is_staff")
    }

    data class UserSnapshot(
        val id: Int,
        val username: String,
        val email: String,
        val isStaff: Boolean
    )

    val userSnapshot: Flow<UserSnapshot?> = context.dataStore.data.map { prefs ->
        val id = prefs[USER_ID] ?: return@map null
        UserSnapshot(
            id       = id,
            username = prefs[USERNAME] ?: "",
            email    = prefs[USER_EMAIL] ?: "",
            isStaff  = prefs[IS_STAFF] ?: false
        )
    }

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS_TOKEN]  = access
            prefs[REFRESH_TOKEN] = refresh
        }
    }

    suspend fun saveUser(id: Int, username: String, email: String, isStaff: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID]    = id
            prefs[USERNAME]   = username
            prefs[USER_EMAIL] = email
            prefs[IS_STAFF]   = isStaff
        }
    }

    suspend fun getAccessToken(): String? = context.dataStore.data.first()[ACCESS_TOKEN]
    suspend fun getRefreshToken(): String? = context.dataStore.data.first()[REFRESH_TOKEN]

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
