package com.svwh.tools.feature.noenvironment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.hook.HookEnvironmentType
import com.svwh.tools.core.hook.HookStateRepository
import com.svwh.tools.constant.ApkConstant
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem
import com.svwh.tools.feature.environment.domain.repository.InstalledAppRepository
import com.svwh.tools.feature.environment.domain.repository.InstalledAppMetaDataFilter
import com.svwh.tools.feature.noenvironment.domain.repack.RepackInstallEventStore
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
    private val installedAppRepository: InstalledAppRepository,
    private val hookStateRepository: HookStateRepository,
    private val repackInstallEventStore: RepackInstallEventStore,
) : ViewModel() {
    private var loadAppsJob: Job? = null

    private val _uiState = MutableStateFlow(NoEnvironmentUiState())
    val uiState: StateFlow<NoEnvironmentUiState> = _uiState.asStateFlow()

    init {
        loadUserApps()
        observeRepackInstallEvents()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(searchQuery = query)
        }
    }

    fun setHookEnabled(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            val actualEnabled = withContext(Dispatchers.IO) {
                hookStateRepository.setHookEnabled(
                    packageName = packageName,
                    environmentType = HookEnvironmentType.NoEnv,
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

    private fun loadUserApps() {
        loadAppsJob?.cancel()
        loadAppsJob = viewModelScope.launch {
            _uiState.update { state -> state.copy(isLoading = true) }

            val apps = withContext(Dispatchers.IO) {
                val loadedApps = installedAppRepository.getInstalledApps(
                    showSystemApps = false,
                    hookedPackages = emptySet(),
                    requiredMetaData = InstalledAppMetaDataFilter(
                        name = ApkConstant.NO_ENV_METADATA_NAME,
                        value = ApkConstant.NO_ENV_METADATA_VALUE,
                    ),
                )
                val enabledPackages = hookStateRepository.syncAndGetEnabledPackages(
                    environmentType = HookEnvironmentType.NoEnv,
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

    private fun observeRepackInstallEvents() {
        viewModelScope.launch {
            repackInstallEventStore.installedPackages
                .collect { packageNames ->
                    packageNames.forEach { packageName ->
                        addInstalledRepackApp(packageName)
                    }
                }
        }
    }

    private fun addInstalledRepackApp(packageName: String) {
        viewModelScope.launch {
            var app: InstalledAppItem? = null
            for (attempt in 0 until 4) {
                app = withContext(Dispatchers.IO) {
                    loadNoEnvironmentApp(packageName)
                }
                if (app != null) break
                if (attempt < 3) delay(300)
            }
            val installedApp = app
            if (installedApp != null) {
                _uiState.update { state ->
                    val withoutOld = state.apps.filterNot { it.packageName == packageName }
                    state.copy(
                        apps = (withoutOld + installedApp).sortedWith(
                            compareByDescending<InstalledAppItem> { it.hookEnabled }
                                .thenByDescending { it.firstInstallTimeMillis },
                        ),
                    )
                }
                repackInstallEventStore.consume(packageName)
            }
        }
    }

    private suspend fun loadNoEnvironmentApp(packageName: String): InstalledAppItem? {
        val loadedApps = installedAppRepository.getInstalledApps(
            showSystemApps = false,
            hookedPackages = emptySet(),
            requiredMetaData = InstalledAppMetaDataFilter(
                name = ApkConstant.NO_ENV_METADATA_NAME,
                value = ApkConstant.NO_ENV_METADATA_VALUE,
            ),
        )
        val app = loadedApps.firstOrNull { it.packageName == packageName } ?: return null
        val enabledPackages = hookStateRepository.syncAndGetEnabledPackages(
            environmentType = HookEnvironmentType.NoEnv,
            packageNames = setOf(packageName),
        )
        return app.copy(hookEnabled = packageName in enabledPackages)
    }
}
