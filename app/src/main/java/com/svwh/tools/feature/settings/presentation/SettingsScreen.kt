package com.svwh.tools.feature.settings.presentation

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svwh.tools.apk.SigningKeyStoreUtils
import com.svwh.tools.core.datastore.RepackSigningMode
import com.svwh.tools.core.datastore.ThemeMode
import com.svwh.tools.core.datastore.UserSettings

private val SettingsBackgroundTop = Color(0xFFF7FAFF)
private val SettingsBackgroundBottom = Color(0xFFFFFFFF)
private val SettingsCardBorder = Color(0xFFE3EAF5)
private val SettingsCardSurface = Color(0xF7FFFFFF)
private val SettingsBlue = Color(0xFF1677FF)
private val SettingsMutedText = Color(0xFF758298)

@Composable
fun SettingsRoute(
    onBack: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onBack = onBack,
        onThemeModeChange = viewModel::setThemeMode,
        onDynamicColorChange = viewModel::setDynamicColor,
        onShowNoEnvironmentTabChange = viewModel::setShowNoEnvironmentTab,
        onShowEnvironmentTabChange = viewModel::setShowEnvironmentTab,
        onDisableCustomSigning = viewModel::disableCustomSigning,
        onSaveCustomSigning = viewModel::saveCustomSigningConfig,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    uiState: UserSettings,
    onBack: (() -> Unit)?,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onShowNoEnvironmentTabChange: (Boolean) -> Unit,
    onShowEnvironmentTabChange: (Boolean) -> Unit,
    onDisableCustomSigning: () -> Unit,
    onSaveCustomSigning: (
        uri: String,
        name: String,
        keyStoreType: String,
        alias: String,
        storePassword: String,
        keyPassword: String,
        onResult: (Result<Unit>) -> Unit,
    ) -> Unit,
) {
    val context = LocalContext.current
    var pendingSigningDraft by remember { mutableStateOf<SigningDraft?>(null) }
    var signingConfigError by remember { mutableStateOf<String?>(null) }
    val signingFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        pendingSigningDraft = uri?.let {
            val name = it.displayName(context)
            val inferredType = SigningKeyStoreUtils.keyStoreTypeFromFileName(name)
            if (inferredType == null) {
                signingConfigError = "请选择 .jks、.bks、.p12 或 .pkcs12 格式的密钥文件"
                return@let null
            }
            signingConfigError = null
            SigningDraft(
                uri = it.toString(),
                name = name,
                keyStoreType = inferredType,
            )
        }
    }
    val launchSigningFilePicker = {
        signingConfigError = null
        signingFileLauncher.launch(
            arrayOf(
                "application/x-bks",
                "application/octet-stream",
                "application/x-java-keystore",
                "application/pkcs12",
                "application/x-pkcs12",
            ),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = "返回",
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(SettingsBackgroundTop, SettingsBackgroundBottom),
                    ),
                )
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SettingsSection(
                title = "外观",
                description = "控制主题色与显示方式",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = uiState.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            label = { Text(mode.label) },
                        )
                    }
                }
                SettingsSwitchRow(
                    title = "动态取色",
                    description = "在支持的设备上使用系统配色",
                    checked = uiState.dynamicColor,
                    onCheckedChange = onDynamicColorChange,
                )
            }

            SettingsSection(
                title = "重打包签名",
                description = "配置 APK 重打包后使用的签名来源",
            ) {
                CustomSigningSwitchRow(
                    enabled = uiState.repackSigningMode == RepackSigningMode.Custom,
                    keyName = uiState.customSigningKeyName,
                    onCheckedChange = { checked ->
                        if (checked) {
                            launchSigningFilePicker()
                        } else {
                            onDisableCustomSigning()
                        }
                    },
                )
                signingConfigError?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (uiState.repackSigningMode == RepackSigningMode.Custom) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = launchSigningFilePicker,
                    ) {
                        Text("更换密钥文件")
                    }
                }
            }

            SettingsSection(
                title = "页面显示",
                description = "控制底部环境页签入口",
            ) {
                SettingsSwitchRow(
                    title = "显示无环境",
                    description = "控制底部栏是否显示无环境页签",
                    checked = uiState.showNoEnvironmentTab,
                    onCheckedChange = onShowNoEnvironmentTabChange,
                )
                HorizontalDivider(color = SettingsCardBorder)
                SettingsSwitchRow(
                    title = "显示有环境",
                    description = "控制底部栏是否显示有环境页签",
                    checked = uiState.showEnvironmentTab,
                    onCheckedChange = onShowEnvironmentTabChange,
                )
                Text(
                    text = "页签显示设置会在重启 App 后生效",
                    style = MaterialTheme.typography.bodySmall,
                    color = SettingsBlue,
                )
            }

            Spacer(modifier = Modifier.padding(bottom = 8.dp))
        }
    }

    pendingSigningDraft?.let { draft ->
        SigningConfigDialog(
            draft = draft,
            onDismiss = { pendingSigningDraft = null },
            onSave = { type, alias, storePassword, keyPassword, onResult ->
                onSaveCustomSigning(
                    draft.uri,
                    draft.name,
                    type,
                    alias,
                    storePassword,
                    keyPassword,
                    onResult,
                )
            },
        )
    }
}

@Composable
private fun CustomSigningSwitchRow(
    enabled: Boolean,
    keyName: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    val description = if (enabled) {
        "将 ${keyName.ifBlank { "自定义密钥" }} 用于打包应用签名阶段"
    } else {
        "开启后可选择自定义密钥用于打包"
    }
    ListItem(
        headlineContent = { Text("采用自定义密钥") },
        supportingContent = { Text(description) },
        trailingContent = {
            Switch(
                checked = enabled,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SigningConfigDialog(
    draft: SigningDraft,
    onDismiss: () -> Unit,
    onSave: (
        type: String,
        alias: String,
        storePassword: String,
        keyPassword: String,
        onResult: (Result<Unit>) -> Unit,
    ) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var keyStoreType by remember(draft.uri) {
        mutableStateOf(SigningKeyStoreUtils.normalizeKeyStoreType(draft.keyStoreType))
    }
    var alias by remember(draft.uri) { mutableStateOf("") }
    var storePassword by remember(draft.uri) { mutableStateOf("") }
    var keyPassword by remember(draft.uri) { mutableStateOf("") }
    var errorMessage by remember(draft.uri) { mutableStateOf<String?>(null) }
    var saving by remember(draft.uri) { mutableStateOf(false) }
    val keyStoreTypes = SigningKeyStoreUtils.supportedKeyStoreTypes

    Dialog(onDismissRequest = {
        if (!saving) {
            onDismiss()
        }
    }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = SettingsCardSurface,
            border = BorderStroke(1.dp, SettingsCardBorder),
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "配置签名密钥",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = draft.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = SettingsMutedText,
                    )
                }
                Column {
                    OutlinedTextField(
                        modifier = Modifier
                            .clickable { expanded = true }
                            .fillMaxWidth(),
                        value = keyStoreType,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("密钥类型") },
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        keyStoreTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    keyStoreType = type
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = storePassword,
                    onValueChange = { storePassword = it },
                    singleLine = true,
                    label = { Text("密钥库密码") },
                    visualTransformation = PasswordVisualTransformation(),
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = alias,
                    onValueChange = { alias = it },
                    singleLine = true,
                    label = { Text("别名") },
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = keyPassword,
                    onValueChange = { keyPassword = it },
                    singleLine = true,
                    label = { Text("别名密码") },
                    visualTransformation = PasswordVisualTransformation(),
                )
                errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        enabled = !saving,
                        onClick = onDismiss,
                    ) {
                        Text("取消")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = !saving,
                        onClick = {
                            errorMessage = null
                            saving = true
                            onSave(
                                keyStoreType,
                                alias,
                                storePassword,
                                keyPassword,
                            ) { result ->
                                saving = false
                                result
                                    .onSuccess { onDismiss() }
                                    .onFailure { throwable ->
                                        errorMessage = throwable.message ?: "签名配置校验失败"
                                    }
                            }
                        },
                    ) {
                        Text(if (saving) "校验中" else "保存")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable {
            onCheckedChange(!checked)
        },
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = description) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@Composable
private fun SettingsSection(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SettingsMutedText,
            )
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = SettingsCardSurface),
            border = BorderStroke(1.dp, SettingsCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                content()
            }
        }
    }
}

private data class SigningDraft(
    val uri: String,
    val name: String,
    val keyStoreType: String,
)

private fun Uri.displayName(context: Context): String {
    val cursor: Cursor? = context.contentResolver.query(this, null, null, null, null)
    return try {
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
        if (cursor != null && cursor.moveToFirst() && nameIndex >= 0) {
            cursor.getString(nameIndex)
        } else {
            lastPathSegment ?: toString()
        }
    } finally {
        cursor?.close()
    }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.FollowSystem -> "跟随系统"
        ThemeMode.Light -> "浅色"
        ThemeMode.Dark -> "深色"
    }
