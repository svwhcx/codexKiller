package com.svwh.tools.feature.hookconfig.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.HookLogPageResult
import com.svwh.tools.feature.hookconfig.domain.model.HookLogQuery
import com.svwh.tools.feature.hookconfig.domain.model.HookLogRecord
import com.svwh.tools.feature.hookconfig.domain.model.HookLogTypeOption
import com.svwh.tools.feature.hookconfig.domain.repository.HookLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class HookLogUiState(
    val envType: String = "",
    val packageName: String = "",
    val searchInput: String = "",
    val submittedKeyword: String = "",
    val logs: List<HookLogRecord> = emptyList(),
    val availableTypes: List<HookLogTypeOption> = emptyList(),
    val selectedTypes: Set<Int> = emptySet(),
    val selectedLog: HookLogRecord? = null,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
internal class HookLogViewModel @Inject constructor(
    private val repository: HookLogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow  (HookLogUiState())
    internal val uiState: StateFlow<HookLogUiState> = _uiState.asStateFlow()

    fun initialize(envType: String, packageName: String) {
        val current = _uiState.value
        if (current.envType == envType && current.packageName == packageName && current.logs.isNotEmpty()) {
            return
        }
        _uiState.value = HookLogUiState(
            envType = envType,
            packageName = packageName,
        )
        refresh()
        loadAvailableTypes()
    }

    fun updateSearchInput(value: String) {
        _uiState.value = _uiState.value.copy(searchInput = value)
    }

    fun submitSearch() {
        _uiState.value = _uiState.value.copy(
            submittedKeyword = _uiState.value.searchInput.trim(),
        )
        refresh()
    }

    fun toggleType(type: Int) {
        val current = _uiState.value.selectedTypes
        val next = if (type in current) current - type else current + type
        _uiState.value = _uiState.value.copy(selectedTypes = next)
        refresh()
    }

    fun applySelectedTypes(types: Set<Int>) {
        _uiState.value = _uiState.value.copy(selectedTypes = types)
        refresh()
    }

    fun openLogDetail(log: HookLogRecord) {
        _uiState.value = _uiState.value.copy(selectedLog = log)
    }

    fun dismissLogDetail() {
        _uiState.value = _uiState.value.copy(selectedLog = null)
    }

    fun refresh() {
        val state = _uiState.value
        if (state.envType.isBlank() || state.packageName.isBlank()) return
        _uiState.value = state.copy(
            isRefreshing = true,
            isLoadingMore = false,
            errorMessage = null,
        )
        viewModelScope.launch {
            when (
                val result = repository.queryLogs(
                    HookLogQuery(
                        envType = state.envType,
                        packageName = state.packageName,
                        keyword = state.submittedKeyword,
                        selectedTypes = state.selectedTypes,
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
                        envType = state.envType,
                        packageName = state.packageName,
                        keyword = state.submittedKeyword,
                        selectedTypes = state.selectedTypes,
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

    private fun loadAvailableTypes() {
        val state = _uiState.value
        if (state.envType.isBlank() || state.packageName.isBlank()) return
        viewModelScope.launch {
            when (
                val result = repository.queryAvailableTypes(
                    envType = state.envType,
                    packageName = state.packageName,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(availableTypes = result.data)
                }
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
