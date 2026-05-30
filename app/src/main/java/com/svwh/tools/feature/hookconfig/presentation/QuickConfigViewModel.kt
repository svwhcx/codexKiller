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
            refresh(envType, packageName, groups)
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
            refresh(state.envType, state.packageName, null)
            _uiState.update { current ->
                current.copy(loadingItems = current.loadingItems - item.id)
            }
        }
    }

    private suspend fun refresh(
        envType: String,
        packageName: String,
        groups: List<HookQuickConfigGroup>?,
    ) {
        val configs = loadConfigs(envType, packageName)
        val runtimeTypesByItemId = groups
            ?.flatMap { group -> group.items }
            ?.mapNotNull { item ->
                val runtimeHookType = item.runtimeHookType ?: return@mapNotNull null
                item.id to runtimeHookType
            }
            ?.toMap()
            ?: QuickHookTypeMapping.all().associate { mapping -> mapping.itemId to mapping.type }
        val enabledTypes = configs
            .asSequence()
            .filter { it.enabled }
            .map { it.type }
            .toSet()
        val enabledItems = runtimeTypesByItemId
            .filterValues { type -> type in enabledTypes }
            .keys
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
        return when (val result = repository.getRuntimeConfigs(envType, packageName)) {
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
            private val cipher = QuickHookTypeMapping(
                itemId = "cipher",
                type = "35",
                configName = "加解密算法",
                className = "javax.crypto.Cipher",
                methodName = "doFinal",
                params = "*",
            )
            private val userCertTrust = QuickHookTypeMapping(
                itemId = "trust_user_cert",
                type = "36",
                configName = "信任用户证书",
                className = "javax.net.ssl.SSLContext",
                methodName = "init",
                params = "*",
            )
            private val hideWifiProxy = QuickHookTypeMapping(
                itemId = "hide_wifi_proxy",
                type = "30",
                configName = "隐藏 Wifi 代理",
                className = "android.net.Proxy",
                methodName = "getDefaultHost",
                params = "*",
            )
            private val hideVpn = QuickHookTypeMapping(
                itemId = "hide_vpn",
                type = "26",
                configName = "隐藏 VPN",
                className = "java.net.NetworkInterface",
                methodName = "getName",
                params = "*",
            )

            private val onClick = QuickHookTypeMapping(
                itemId = "onclick",
                type = "5",
                configName = "onClick 监听",
                className = "android.view.View",
                methodName = "setOnClickListener",
                params = "android.view.View\$OnClickListener",
            )

            fun all(): List<QuickHookTypeMapping> = listOf(
                digest,
                cipher,
                userCertTrust,
                hideWifiProxy,
                hideVpn,
                onClick,
            )

            fun fromItemId(itemId: String): QuickHookTypeMapping? {
                return all().firstOrNull { it.itemId == itemId }
            }
        }
    }
}
