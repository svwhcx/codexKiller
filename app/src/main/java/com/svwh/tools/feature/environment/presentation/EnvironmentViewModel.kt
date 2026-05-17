package com.svwh.tools.feature.environment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.environment.LsposedStatus
import com.svwh.tools.core.hook.HookEnvironmentType
import com.svwh.tools.core.hook.HookStateRepository
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
    private val installedAppRepository: InstalledAppRepository,
    private val hookStateRepository: HookStateRepository,
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
            val actualEnabled = withContext(Dispatchers.IO) {
                hookStateRepository.setHookEnabled(
                    packageName = packageName,
                    environmentType = HookEnvironmentType.WithEnv,
                    enabled = enabled,
                )
            }
            _uiState.update { state ->
                state.copy(
                    apps = state.apps.map { app ->
                        if (app.packageName == packageName) {
                            app.copy(hookEnabled = actualEnabled)
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
                val loadedApps = installedAppRepository.getInstalledApps(
                    showSystemApps = showSystemApps,
                    hookedPackages = emptySet(),
                )
                val enabledPackages = hookStateRepository.syncAndGetEnabledPackages(
                    environmentType = HookEnvironmentType.WithEnv,
                    packageNames = loadedApps.map { it.packageName }.toSet(),
                )
                delay(450)
                loadedApps
                    .map { app ->
                        app.copy(hookEnabled = app.packageName in enabledPackages)
                    }
                    .sortedWith(
                        compareByDescending<InstalledAppItem> { it.hookEnabled }
                            .thenByDescending { it.firstInstallTimeMillis },
                    )
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
