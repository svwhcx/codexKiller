package com.svwh.tools.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
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
                fridaLogCompactStyle = preferences[Keys.FridaLogCompactStyle] ?: false,
                repackSigningMode = preferences[Keys.RepackSigningMode]
                    ?.let(::runCatchingRepackSigningMode)
                    ?: RepackSigningMode.BuiltIn,
                customSigningKeyUri = preferences[Keys.CustomSigningKeyUri].orEmpty(),
                customSigningKeyName = preferences[Keys.CustomSigningKeyName].orEmpty(),
                customSigningKeyStoreType = preferences[Keys.CustomSigningKeyStoreType] ?: "BKS",
                customSigningKeyAlias = preferences[Keys.CustomSigningKeyAlias].orEmpty(),
                customSigningStorePassword = preferences[Keys.CustomSigningStorePassword].orEmpty(),
                customSigningKeyPassword = preferences[Keys.CustomSigningKeyPassword].orEmpty(),
            )
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

    suspend fun setFridaLogCompactStyle(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.FridaLogCompactStyle] = enabled
        }
    }

    suspend fun setBuiltInRepackSigning() {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.RepackSigningMode] = RepackSigningMode.BuiltIn.name
        }
    }

    suspend fun setCustomSigningConfig(
        uri: String,
        name: String,
        keyStoreType: String,
        alias: String,
        storePassword: String,
        keyPassword: String,
    ) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.RepackSigningMode] = RepackSigningMode.Custom.name
            preferences[Keys.CustomSigningKeyUri] = uri
            preferences[Keys.CustomSigningKeyName] = name
            preferences[Keys.CustomSigningKeyStoreType] = keyStoreType
            preferences[Keys.CustomSigningKeyAlias] = alias
            preferences[Keys.CustomSigningStorePassword] = storePassword
            preferences[Keys.CustomSigningKeyPassword] = keyPassword
        }
    }

    private fun runCatchingThemeMode(value: String): ThemeMode? {
        return runCatching { ThemeMode.valueOf(value) }.getOrNull()
    }

    private fun runCatchingRepackSigningMode(value: String): RepackSigningMode? {
        return runCatching { RepackSigningMode.valueOf(value) }.getOrNull()
    }

    private object Keys {
        val ThemeMode = stringPreferencesKey("theme_mode")
        val DynamicColor = booleanPreferencesKey("dynamic_color")
        val ShowNoEnvironmentTab = booleanPreferencesKey("show_no_environment_tab")
        val ShowEnvironmentTab = booleanPreferencesKey("show_environment_tab")
        val FridaLogCompactStyle = booleanPreferencesKey("frida_log_compact_style")
        val RepackSigningMode = stringPreferencesKey("repack_signing_mode")
        val CustomSigningKeyUri = stringPreferencesKey("custom_signing_key_uri")
        val CustomSigningKeyName = stringPreferencesKey("custom_signing_key_name")
        val CustomSigningKeyStoreType = stringPreferencesKey("custom_signing_key_store_type")
        val CustomSigningKeyAlias = stringPreferencesKey("custom_signing_key_alias")
        val CustomSigningStorePassword = stringPreferencesKey("custom_signing_store_password")
        val CustomSigningKeyPassword = stringPreferencesKey("custom_signing_key_password")
    }
}
