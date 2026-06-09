package com.svwh.tools.feature.noenvironment.presentation.repack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem
import com.svwh.tools.feature.environment.domain.repository.InstalledAppRepository
import com.svwh.tools.feature.noenvironment.domain.model.RepackProgressState
import com.svwh.tools.feature.noenvironment.domain.repack.RepackInstallEventStore
import com.svwh.tools.feature.noenvironment.domain.repack.RepackProgressController
import com.svwh.tools.feature.noenvironment.domain.repack.RepackWorkflowRunner
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RepackAppListUiState(
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

            return filtered.sortedByDescending { it.firstInstallTimeMillis }
        }
}

@HiltViewModel
class RepackAppListViewModel @Inject constructor(
    private val installedAppRepository: InstalledAppRepository,
    private val repackInstallEventStore: RepackInstallEventStore,
    private val repackProgressController: RepackProgressController,
    private val repackWorkflowRunner: RepackWorkflowRunner,
) : ViewModel() {
    private var loadAppsJob: Job? = null
    private var repackJob: Job? = null

    private val _uiState = MutableStateFlow(RepackAppListUiState())
    val uiState: StateFlow<RepackAppListUiState> = _uiState.asStateFlow()

    val repackProgressState: StateFlow<RepackProgressState> = repackProgressController.state

    init {
        loadUserApps()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(searchQuery = query)
        }
    }

    fun onRepackClick(packageName: String) {
        val app = _uiState.value.apps.find { it.packageName == packageName } ?: return
        startRepackWorkflow(app)
    }

    fun dismissRepackProgress() {
        repackProgressController.dismiss()
    }

    fun setRepackPageVisible(visible: Boolean) {
        repackProgressController.setScreenVisible(visible)
    }

    fun onInstallSucceeded(packageName: String) {
        repackInstallEventStore.markInstalled(packageName)
        _uiState.update { state ->
            state.copy(
                apps = state.apps.filterNot { app -> app.packageName == packageName },
            )
        }
    }

    fun stopRepackProgress() {
        repackJob?.cancel()
        repackJob = null
        repackWorkflowRunner.stop()
        repackProgressController.stopSession()
    }

    fun installRepackResult() {
        // Reserved for install output apk flow.
    }

    fun showRepackDetails() {
        // Reserved for failure details screen.
    }

    private fun startRepackWorkflow(app: InstalledAppItem) {
        repackJob?.cancel()
        repackJob = viewModelScope.launch {
            repackWorkflowRunner.start(app)
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
                )
                delay(450)
                loadedApps.sortedByDescending { it.firstInstallTimeMillis }
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
