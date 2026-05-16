package com.svwh.tools.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "stool_settings",
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val userSettings: Flow<UserSettings> = context.settingsDataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            UserSettings(
                themeMode = preferences[Keys.ThemeMode]
                    ?.let(::runCatchingThemeMode)
                    ?: ThemeMode.FollowSystem,
                dynamicColor = preferences[Keys.DynamicColor] ?: true,
                showNoEnvironmentTab = preferences[Keys.ShowNoEnvironmentTab] ?: true,
                showEnvironmentTab = preferences[Keys.ShowEnvironmentTab] ?: true,
            )
        }

    val hookedPackages: Flow<Set<String>> = context.settingsDataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            preferences[Keys.HookedPackages] ?: emptySet()
        }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.ThemeMode] = themeMode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.DynamicColor] = enabled
        }
    }

    suspend fun setShowNoEnvironmentTab(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.ShowNoEnvironmentTab] = enabled
        }
    }

    suspend fun setShowEnvironmentTab(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.ShowEnvironmentTab] = enabled
        }
    }

    suspend fun setPackageHookEnabled(packageName: String, enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            val currentPackages = preferences[Keys.HookedPackages].orEmpty()
            preferences[Keys.HookedPackages] = if (enabled) {
                currentPackages + packageName
            } else {
                currentPackages - packageName
            }
        }
    }

    private fun runCatchingThemeMode(value: String): ThemeMode? {
        return runCatching { ThemeMode.valueOf(value) }.getOrNull()
    }

    private object Keys {
        val ThemeMode = stringPreferencesKey("theme_mode")
        val DynamicColor = booleanPreferencesKey("dynamic_color")
        val ShowNoEnvironmentTab = booleanPreferencesKey("show_no_environment_tab")
        val ShowEnvironmentTab = booleanPreferencesKey("show_environment_tab")
        val HookedPackages = stringSetPreferencesKey("hooked_packages")
    }
}
