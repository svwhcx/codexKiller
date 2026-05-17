package com.svwh.tools.feature.environment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.datastore.SettingsDataStore
import com.svwh.tools.core.environment.LsposedStatus
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem
import com.svwh.tools.feature.environment.domain.repository.InstalledAppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class EnvironmentUiState(
    val isLoading: Boolean = true,
    val showSystemApps: Boolean = false,
    val searchQuery: String = "",
    val lsposedEnabled: Boolean = false,
    val apps: List<InstalledAppItem> = emptyList(),
) {
    val filteredApps: List<InstalledAppItem>
        get() {
            val keyword = searchQuery.trim()
            val filtered = if (keyword.isEmpty()) {
                apps
            } else {
                apps.filter { app ->
                    app.appName.contains(keyword, ignoreCase = true) ||
                        app.packageName.contains(keyword, ignoreCase = true)
                }
            }

            return filtered
        }
}

@HiltViewModel
class EnvironmentViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val installedAppRepository: InstalledAppRepository,
) : ViewModel() {
    private var loadAppsJob: Job? = null

    private val _uiState = MutableStateFlow(
        EnvironmentUiState(
            lsposedEnabled = LsposedStatus.isEnabled,
        ),
    )
    val uiState: StateFlow<EnvironmentUiState> = _uiState.asStateFlow()

    init {
        loadApps(showSystemApps = false)
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(searchQuery = query)
        }
    }

    fun setShowSystemApps(showSystemApps: Boolean) {
        _uiState.update { state ->
            state.copy(showSystemApps = showSystemApps)
        }
        loadApps(showSystemApps = showSystemApps)
    }

    fun setHookEnabled(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setPackageHookEnabled(packageName, enabled)
            _uiState.update { state ->
                state.copy(
                    apps = state.apps.map { app ->
                        if (app.packageName == packageName) {
                            app.copy(hookEnabled = enabled)
                        } else {
                            app
                        }
                    },
                )
            }
        }
    }

    private fun loadApps(showSystemApps: Boolean) {
        loadAppsJob?.cancel()
        loadAppsJob = viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
                    showSystemApps = showSystemApps,
                    lsposedEnabled = LsposedStatus.isEnabled,
                )
            }

            val apps = withContext(Dispatchers.IO) {
                val hookedPackages = settingsDataStore.hookedPackages.first()
                val loadedApps = installedAppRepository.getInstalledApps(
                    showSystemApps = showSystemApps,
                    hookedPackages = hookedPackages,
                )
                delay(450)
                loadedApps
            }

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    apps = apps,
                )
            }
        }
    }

}
