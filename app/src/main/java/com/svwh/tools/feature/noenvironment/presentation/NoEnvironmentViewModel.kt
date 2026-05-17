package com.svwh.tools.feature.noenvironment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.datastore.SettingsDataStore
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

data class NoEnvironmentUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
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
class NoEnvironmentViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val installedAppRepository: InstalledAppRepository,
) : ViewModel() {
    private var loadAppsJob: Job? = null

    private val _uiState = MutableStateFlow(NoEnvironmentUiState())
    val uiState: StateFlow<NoEnvironmentUiState> = _uiState.asStateFlow()

    init {
        loadUserApps()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(searchQuery = query)
        }
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

    private fun loadUserApps() {
        loadAppsJob?.cancel()
        loadAppsJob = viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true) }

            val apps = withContext(Dispatchers.IO) {
                val hookedPackages = settingsDataStore.hookedPackages.first()
                val loadedApps = installedAppRepository.getInstalledApps(
                    showSystemApps = false,
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
