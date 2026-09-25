package com.siaka.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    fun profileImagePath(userId: String): Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[stringPreferencesKey("profile_image_$userId")] }

    suspend fun setProfileImagePath(userId: String, path: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("profile_image_$userId")] = path
        }
    }

    suspend fun clearProfileImagePath(userId: String) {
        context.dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey("profile_image_$userId"))
        }
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        //Map and catch exceptions
        //If there is an exception, return empty preferences
        //If there is an IOException, return empty preferences
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }
}
