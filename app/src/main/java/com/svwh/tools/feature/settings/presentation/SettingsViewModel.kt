package com.svwh.tools.feature.settings.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svwh.tools.apk.SigningKeyStoreUtils
import com.svwh.tools.core.datastore.SettingsDataStore
import com.svwh.tools.core.datastore.ThemeMode
import com.svwh.tools.core.datastore.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {
    val uiState: StateFlow<UserSettings> = settingsDataStore.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserSettings(),
        )

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsDataStore.setThemeMode(themeMode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDynamicColor(enabled)
        }
    }

    fun setShowNoEnvironmentTab(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setShowNoEnvironmentTab(enabled)
        }
    }

    fun setShowEnvironmentTab(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setShowEnvironmentTab(enabled)
        }
    }

    fun setFridaLogCompactStyle(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setFridaLogCompactStyle(enabled)
        }
    }

    fun disableCustomSigning() {
        viewModelScope.launch {
            settingsDataStore.setBuiltInRepackSigning()
        }
    }

    fun saveCustomSigningConfig(
        uri: String,
        name: String,
        keyStoreType: String,
        alias: String,
        storePassword: String,
        keyPassword: String,
        onResult: (Result<Unit>) -> Unit,
    ) {
        viewModelScope.launch {
            onResult(runCatching {
                val resolvedKeyStoreType = validateCustomSigningConfig(
                    uri = uri,
                    name = name,
                    keyStoreType = keyStoreType,
                    alias = alias,
                    storePassword = storePassword,
                    keyPassword = keyPassword,
                )
                context.contentResolver.takePersistableUriPermission(
                    Uri.parse(uri),
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
                settingsDataStore.setCustomSigningConfig(
                    uri = uri,
                    name = name,
                    keyStoreType = resolvedKeyStoreType,
                    alias = alias,
                    storePassword = storePassword,
                    keyPassword = keyPassword,
                )
            })
        }
    }

    private fun validateCustomSigningConfig(
        uri: String,
        name: String,
        keyStoreType: String,
        alias: String,
        storePassword: String,
        keyPassword: String,
    ): String {
        require(uri.isNotBlank()) { "请先选择签名密钥文件" }
        require(SigningKeyStoreUtils.isSupportedSigningKeyName(name)) {
            "请选择 .jks、.bks、.p12 或 .pkcs12 格式的密钥文件"
        }
        require(alias.isNotBlank()) { "请填写密钥别名" }
        require(storePassword.isNotBlank()) { "请填写密钥库密码" }
        require(keyPassword.isNotBlank()) { "请填写别名密码" }
        val keyStoreBytes = context.contentResolver.openInputStream(Uri.parse(uri))
            ?.use { it.readBytes() }
            ?: error("无法打开签名密钥文件")
        val loadedKeyStore = SigningKeyStoreUtils.loadKeyStore(
            keyStoreBytes = keyStoreBytes,
            preferredType = keyStoreType,
            storePassword = storePassword.toCharArray(),
        )
        val keyStore = loadedKeyStore.keyStore
        require(keyStore.containsAlias(alias)) { "密钥库中不存在该别名" }
        val entry = runCatching {
            keyStore.getEntry(
                alias,
                KeyStore.PasswordProtection(keyPassword.toCharArray()),
            )
        }.getOrElse {
            throw IllegalArgumentException("别名密码不正确，或该别名无法读取私钥", it)
        }
        require(entry is KeyStore.PrivateKeyEntry) { "选择的别名不是私钥条目" }
        return loadedKeyStore.type
    }
}
