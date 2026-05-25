package com.svwh.tools.feature.hookconfig.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigDraft
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigItem
import com.svwh.tools.feature.hookconfig.domain.repository.UserHookConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class QuickConfigUiState(
    val envType: String = "",
    val packageName: String = "",
    val enabledItems: Set<String> = emptySet(),
    val loadingItems: Set<String> = emptySet(),
)

@HiltViewModel
internal class QuickConfigViewModel @Inject constructor(
    private val repository: UserHookConfigRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickConfigUiState())
    val uiState: StateFlow<QuickConfigUiState> = _uiState.asStateFlow()

    fun initialize(
        envType: String,
        packageName: String,
        groups: List<HookQuickConfigGroup>,
    ) {
        val current = _uiState.value
        if (current.envType == envType && current.packageName == packageName) return
        _uiState.value = QuickConfigUiState(envType = envType, packageName = packageName)
        viewModelScope.launch {
            ensureDefaultConfigs(envType, packageName, groups)
            refresh(envType, packageName)
        }
    }

    fun updateQuickConfig(item: HookQuickConfigItem, enabled: Boolean) {
        val mapping = QuickHookTypeMapping.fromItemId(item.id) ?: return
        val state = _uiState.value
        if (state.envType.isBlank() || state.packageName.isBlank()) return

        _uiState.update { current ->
            current.copy(
                enabledItems = if (enabled) {
                    current.enabledItems + item.id
                } else {
                    current.enabledItems - item.id
                },
                loadingItems = current.loadingItems + item.id,
            )
        }

        viewModelScope.launch {
            val configs = loadConfigs(state.envType, state.packageName)
            val existing = configs.firstOrNull { it.type == mapping.type }
            if (existing != null) {
                repository.updateEnabled(existing.id, enabled)
            } else if (enabled) {
                repository.saveConfig(mapping.toDraft(state.envType, state.packageName))
            }
            refresh(state.envType, state.packageName)
            _uiState.update { current ->
                current.copy(loadingItems = current.loadingItems - item.id)
            }
        }
    }

    private suspend fun ensureDefaultConfigs(
        envType: String,
        packageName: String,
        groups: List<HookQuickConfigGroup>,
    ) {
        val configs = loadConfigs(envType, packageName)
        groups.flatMap { it.items }
            .filter { it.enabledByDefault }
            .mapNotNull { item -> QuickHookTypeMapping.fromItemId(item.id)?.to(item) }
            .forEach { (mapping, _) ->
                val existing = configs.firstOrNull { it.type == mapping.type }
                if (existing == null) {
                    repository.saveConfig(mapping.toDraft(envType, packageName))
                }
            }
    }

    private suspend fun refresh(envType: String, packageName: String) {
        val configs = loadConfigs(envType, packageName)
        val enabledItems = QuickHookTypeMapping.all()
            .filter { mapping -> configs.any { it.type == mapping.type && it.enabled } }
            .map { it.itemId }
            .toSet()
        _uiState.update { current ->
            current.copy(
                envType = envType,
                packageName = packageName,
                enabledItems = enabledItems,
            )
        }
    }

    private suspend fun loadConfigs(envType: String, packageName: String): List<UserHookConfigItem> {
        return when (val result = repository.getConfigs(envType, packageName)) {
            is AppResult.Success -> result.data
            is AppResult.Failure -> emptyList()
        }
    }

    private data class QuickHookTypeMapping(
        val itemId: String,
        val type: String,
        val configName: String,
        val className: String,
        val methodName: String,
        val params: String,
    ) {
        fun toDraft(envType: String, packageName: String): UserHookConfigDraft {
            return UserHookConfigDraft(
                packageName = packageName,
                envType = envType,
                configName = configName,
                className = className,
                methodName = methodName,
                params = params,
                methodSignature = "$className#$methodName($params)",
                hookStatus = true,
                isLog = true,
                isInterrupted = false,
                enabled = true,
                type = type,
            )
        }

        companion object {
            private val digest = QuickHookTypeMapping(
                itemId = "digest",
                type = "34",
                configName = "摘要算法",
                className = "java.security.MessageDigest",
                methodName = "digest",
                params = "*",
            )

            fun all(): List<QuickHookTypeMapping> = listOf(digest)

            fun fromItemId(itemId: String): QuickHookTypeMapping? {
                return all().firstOrNull { it.itemId == itemId }
            }
        }
    }
}
