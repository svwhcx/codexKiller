package com.svwh.tools.feature.noenvironment.presentation.repack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
) : ViewModel() {
    private var loadAppsJob: Job? = null

    private val _uiState = MutableStateFlow(RepackAppListUiState())
    val uiState: StateFlow<RepackAppListUiState> = _uiState.asStateFlow()

    init {
        loadUserApps()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(searchQuery = query)
        }
    }

    fun onRepackClick(packageName: String) {
        // Reserved for the repack workflow screen.
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
