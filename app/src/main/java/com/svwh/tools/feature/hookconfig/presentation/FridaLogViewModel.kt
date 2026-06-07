package com.svwh.tools.feature.hookconfig.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.HookLogPageResult
import com.svwh.tools.feature.hookconfig.domain.model.HookLogQuery
import com.svwh.tools.feature.hookconfig.domain.model.HookLogRecord
import com.svwh.tools.feature.hookconfig.domain.model.HookLogTypeOption
import com.svwh.tools.feature.hookconfig.domain.repository.FridaLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class FridaLogUiState(
    val packageName: String = "",
    val searchInput: String = "",
    val submittedKeyword: String = "",
    val logs: List<HookLogRecord> = emptyList(),
    val availableLevels: List<HookLogTypeOption> = emptyList(),
    val selectedLevels: Set<Int> = emptySet(),
    val selectedIds: Set<Long> = emptySet(),
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null,
) {
    val isSelectionMode: Boolean
        get() = selectedIds.isNotEmpty()
}

@HiltViewModel
internal class FridaLogViewModel @Inject constructor(
    private val repository: FridaLogRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FridaLogUiState())
    internal val uiState: StateFlow<FridaLogUiState> = _uiState.asStateFlow()

    fun initialize(packageName: String) {
        val current = _uiState.value
        if (current.packageName == packageName && current.logs.isNotEmpty()) return
        _uiState.value = FridaLogUiState(packageName = packageName)
        refresh()
        loadAvailableLevels()
    }

    fun updateSearchInput(value: String) {
        _uiState.value = _uiState.value.copy(searchInput = value)
    }

    fun submitSearch() {
        _uiState.value = _uiState.value.copy(
            submittedKeyword = _uiState.value.searchInput.trim(),
            selectedIds = emptySet(),
        )
        refresh()
    }

    fun applySelectedLevels(levels: Set<Int>) {
        _uiState.value = _uiState.value.copy(selectedLevels = levels, selectedIds = emptySet())
        refresh()
    }

    fun startSelection(id: Long) {
        _uiState.value = _uiState.value.copy(selectedIds = setOf(id))
    }

    fun toggleSelected(id: Long) {
        val selected = _uiState.value.selectedIds
        val next = if (id in selected) selected - id else selected + id
        _uiState.value = _uiState.value.copy(selectedIds = next)
    }

    fun selectAllVisible() {
        _uiState.value = _uiState.value.copy(
            selectedIds = _uiState.value.logs.map { it.id }.toSet(),
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedIds = emptySet())
    }

    fun deleteSelectedLogs() {
        val state = _uiState.value
        val ids = state.selectedIds
        if (ids.isEmpty()) return
        viewModelScope.launch {
            when (val result = repository.deleteByIds(state.packageName, ids)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(selectedIds = emptySet())
                    refresh()
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.error.toString())
                }
            }
        }
    }

    fun clearLogs() {
        val state = _uiState.value
        viewModelScope.launch {
            when (val result = repository.deleteAll(state.packageName, state.selectedLevels)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(selectedIds = emptySet())
                    refresh()
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(errorMessage = result.error.toString())
                }
            }
        }
    }

    fun refresh() {
        val state = _uiState.value
        if (state.packageName.isBlank()) return
        _uiState.value = state.copy(
            isRefreshing = true,
            isLoadingMore = false,
            errorMessage = null,
        )
        viewModelScope.launch {
            when (
                val result = repository.queryLogs(
                    HookLogQuery(
                        envType = "no_env",
                        packageName = state.packageName,
                        keyword = state.submittedKeyword,
                        selectedTypes = state.selectedLevels,
                        offset = 0,
                    )
                )
            ) {
                is AppResult.Success -> applyFirstPage(result.data)
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        logs = emptyList(),
                        isRefreshing = false,
                        hasMore = false,
                        isEmpty = true,
                        errorMessage = result.error.toString(),
                    )
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isRefreshing || state.isLoadingMore || !state.hasMore) return
        _uiState.value = state.copy(isLoadingMore = true)
        viewModelScope.launch {
            when (
                val result = repository.queryLogs(
                    HookLogQuery(
                        envType = "frida",
                        packageName = state.packageName,
                        keyword = state.submittedKeyword,
                        selectedTypes = state.selectedLevels,
                        offset = state.logs.size,
                    )
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        logs = state.logs + result.data.items,
                        isLoadingMore = false,
                        hasMore = result.data.hasMore,
                        isEmpty = state.logs.isEmpty() && result.data.items.isEmpty(),
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        errorMessage = result.error.toString(),
                    )
                }
            }
        }
    }

    private fun loadAvailableLevels() {
        viewModelScope.launch {
            when (val result = repository.queryAvailableLevels()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(availableLevels = result.data)
                is AppResult.Failure -> Unit
            }
        }
    }

    private fun applyFirstPage(result: HookLogPageResult) {
        _uiState.value = _uiState.value.copy(
            logs = result.items,
            isRefreshing = false,
            hasMore = result.hasMore,
            isEmpty = result.items.isEmpty(),
            errorMessage = null,
        )
    }
}
