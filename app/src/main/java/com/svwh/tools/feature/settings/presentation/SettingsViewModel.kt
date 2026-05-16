package com.svwh.tools.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.datastore.SettingsDataStore
import com.svwh.tools.core.datastore.ThemeMode
import com.svwh.tools.core.datastore.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val uiState: StateFlow<UserSettings> = settingsDataStore.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSettings(),
        )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(themeMode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDynamicColor(enabled)
        }
    }

    fun setShowNoEnvironmentTab(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setShowNoEnvironmentTab(enabled)
        }
    }

    fun setShowEnvironmentTab(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setShowEnvironmentTab(enabled)
        }
    }
}
