package com.svwh.tools.feature.settings.presentation

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svwh.tools.apk.SigningKeyStoreUtils
import com.svwh.tools.core.datastore.RepackSigningMode
import com.svwh.tools.core.datastore.ThemeMode
import com.svwh.tools.core.datastore.UserSettings

private val SettingsBackgroundTop = Color(0xFFF6F8FC)
private val SettingsBackgroundBottom = Color(0xFFFFFFFF)
private val SettingsCardBorder = Color(0xFFE8EDF5)
private val SettingsCardSurface = Color(0xFFFFFFFF)
private val SettingsBlue = Color(0xFF1677FF)
private val SettingsPaleBlue = Color(0xFFEAF1FF)
private val SettingsTitleText = Color(0xFF182235)
private val SettingsMutedText = Color(0xFF7B8798)

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
        containerColor = SettingsBackgroundBottom,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = SettingsTitleText,
                    navigationIconContentColor = SettingsTitleText,
                    actionIconContentColor = SettingsTitleText,
                ),
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SettingsSection(
                title = "外观",
                description = "控制主题色与显示方式",
            ) {
                SegmentedThemeSelector(
                    selectedMode = uiState.themeMode,
                    onThemeModeChange = onThemeModeChange,
                )
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
                    SectionHintText(text = message, color = MaterialTheme.colorScheme.error)
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
                SectionHintText(
                    text = "页签显示设置会在重启 App 后生效",
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
private fun SegmentedThemeSelector(
    selectedMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SettingsCardBorder, RoundedCornerShape(14.dp)),
    ) {
        ThemeMode.entries.forEachIndexed { index, mode ->
            val selected = selectedMode == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if (selected) SettingsPaleBlue else SettingsCardSurface)
                    .clickable { onThemeModeChange(mode) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) SettingsBlue else SettingsMutedText,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                )
            }
            if (index != ThemeMode.entries.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(SettingsCardBorder),
                )
            }
        }
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
    SettingsToggleRow(
        title = "采用自定义密钥",
        description = description,
        checked = enabled,
        onCheckedChange = onCheckedChange,
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
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        value = keyStoreType,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("密钥类型") },
                        trailingIcon = {
                            Text(
                                modifier = Modifier.clickable { expanded = true },
                                text = if (expanded) "▲" else "▼",
                                color = SettingsMutedText,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        enabled = true,
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
    SettingsToggleRow(
        title = title,
        description = description,
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = SettingsTitleText,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SettingsMutedText,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SettingsBlue,
                checkedBorderColor = SettingsBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5EAF2),
                uncheckedBorderColor = Color(0xFFD5DCE8),
            ),
        )
    }
}

@Composable
private fun SectionHintText(
    text: String,
    color: Color,
) {
    Text(
        modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        fontWeight = FontWeight.Medium,
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = SettingsTitleText,
                fontWeight = FontWeight.SemiBold,
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
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
