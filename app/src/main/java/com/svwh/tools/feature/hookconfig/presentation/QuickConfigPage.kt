package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.svwh.tools.core.permission.rememberExternalStoragePermissionGate
import com.svwh.tools.feature.environment.presentation.HookSwitch

private val QuickConfigGroupIconBackground = Color(0xFFEAF3FF)
private val QuickConfigTitleColor = Color(0xFF14213A)
private val QuickConfigLineColor = Color(0xFFE6ECF5)
private val QuickConfigDialogField = Color(0xFFF3F6FB)
private val QuickConfigDialogBorder = Color(0xFFE2E8F2)
private val QuickConfigDialogText = Color(0xFF374151)

@Composable
internal fun QuickConfigPage(
    envType: String,
    packageName: String,
    onPermissionDenied: () -> Unit = {},
    viewModel: QuickConfigViewModel = hiltViewModel(),
) {
    val groups = remember { defaultQuickConfigGroups() }
    val uiState by viewModel.uiState.collectAsState()
    val storagePermissionGate = rememberExternalStoragePermissionGate(onPermissionDenied)
    var showFridaDelayDialog by remember { mutableStateOf(false) }

    LaunchedEffect(envType, packageName) {
        viewModel.initialize(
            envType = envType,
            packageName = packageName,
            groups = groups,
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 10.dp, bottom = 18.dp),
    ) {
        items(groups, key = { it.id }) { group ->
            QuickConfigGroupSection(
                group = group,
                uiState = uiState,
                onCheckedChange = { item, enabled ->
                    storagePermissionGate.runAfterPermission {
                        viewModel.updateQuickConfig(item, enabled)
                    }
                },
                onActionClick = { item ->
                    if (item.actionType == HookQuickConfigActionType.FridaDelayInject) {
                        storagePermissionGate.runAfterPermission {
                            showFridaDelayDialog = true
                        }
                    }
                },
            )
        }
    }

    if (showFridaDelayDialog) {
        FridaDelayInjectDialog(
            initialDelayMillis = uiState.fridaDelayInjectMillis ?: 0L,
            errorMessage = uiState.fridaConfigError,
            saving = uiState.fridaConfigSaving,
            onDismiss = { showFridaDelayDialog = false },
            onConfirm = { delayMillis ->
                viewModel.saveFridaDelayInjectMillis(delayMillis)
                showFridaDelayDialog = false
            },
        )
    }
}

@Composable
private fun QuickConfigGroupSection(
    group: HookQuickConfigGroup,
    uiState: QuickConfigUiState,
    onCheckedChange: (HookQuickConfigItem, Boolean) -> Unit,
    onActionClick: (HookQuickConfigItem) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        QuickConfigGroupHeader(group = group)
        Spacer(modifier = Modifier.height(8.dp))
        group.items.forEachIndexed { index, item ->
            QuickConfigItemRow(
                item = item,
                checked = item.id in uiState.enabledItems,
                enabled = when (item.actionType) {
                    HookQuickConfigActionType.FridaDelayInject -> uiState.envType == "no_env"
                    HookQuickConfigActionType.Toggle -> item.runtimeHookType != null &&
                        item.id !in uiState.loadingItems
                },
                valueText = when (item.actionType) {
                    HookQuickConfigActionType.FridaDelayInject -> uiState.fridaDelayInjectMillis
                        ?.let { "$it ms" }
                    HookQuickConfigActionType.Toggle -> null
                },
                onCheckedChange = onCheckedChange,
                onActionClick = onActionClick,
            )
            if (index != group.items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    color = QuickConfigLineColor,
                    thickness = 0.7.dp,
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun QuickConfigGroupHeader(group: HookQuickConfigGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(QuickConfigGroupIconBackground, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = group.icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = HookConfigPrimaryBlue,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.bodyLarge,
                color = QuickConfigTitleColor,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = group.description,
                style = MaterialTheme.typography.bodySmall,
                color = HookConfigMutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuickConfigItemRow(
    item: HookQuickConfigItem,
    checked: Boolean,
    enabled: Boolean,
    valueText: String?,
    onCheckedChange: (HookQuickConfigItem, Boolean) -> Unit,
    onActionClick: (HookQuickConfigItem) -> Unit,
) {
    val isActionItem = item.actionType != HookQuickConfigActionType.Toggle
    val hasDetails = item.summaryValues.isNotEmpty() || item.detailHint != null || isActionItem

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                if (isActionItem) {
                    onActionClick(item)
                } else {
                    onCheckedChange(item, !checked)
                }
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = QuickConfigTitleColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.subtitle != null) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = HookConfigMutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (valueText != null) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodySmall,
                    color = HookConfigPrimaryBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp, end = 6.dp),
                )
            }
            if (hasDetails) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = HookConfigMutedText,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            if (!isActionItem) {
                HookSwitch(
                    checked = checked,
                    onCheckedChange = {
                        if (enabled) {
                            onCheckedChange(item, it)
                        }
                    },
                )
            }
        }

        val detailText = when {
            item.subtitle != null -> null
            item.summaryValues.isNotEmpty() -> item.summaryValues.take(3).joinToString(", ")
            else -> item.detailHint
        }
        if (detailText != null) {
            Text(
                text = detailText,
                modifier = Modifier.padding(end = 52.dp),
                style = MaterialTheme.typography.bodySmall,
                color = HookConfigMutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FridaDelayInjectDialog(
    initialDelayMillis: Long,
    errorMessage: String?,
    saving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    var input by remember(initialDelayMillis) { mutableStateOf(initialDelayMillis.toString()) }
    val parsedDelay = input.toLongOrNull()
    val localError = when {
        input.isBlank() -> "请输入延迟时间"
        parsedDelay == null -> "只能输入整数"
        parsedDelay < 0 -> "延迟时间必须 >= 0"
        else -> null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 14.dp),
                ) {
                    Text(
                        text = "延迟注入",
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = QuickConfigTitleColor,
                    )
                }

                Text(
                    text = "填写 native hook 库加载前等待的时间",
                    color = HookConfigMutedText,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )

                FridaDelayInputField(
                    value = input,
                    isError = localError != null,
                    onValueChange = { value ->
                        input = value.filter { char -> char.isDigit() }
                    },
                )

                val message = localError ?: errorMessage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .padding(top = 6.dp),
                ) {
                    if (message != null) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .requiredHeight(42.dp),
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = QuickConfigDialogField,
                            contentColor = Color(0xFF4B5563),
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Text(
                            text = "取消",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .requiredHeight(42.dp),
                        enabled = !saving && localError == null && parsedDelay != null,
                        onClick = {
                            if (parsedDelay != null) {
                                onConfirm(parsedDelay)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HookConfigPrimaryBlue,
                            contentColor = Color.White,
                            disabledContainerColor = HookConfigPrimaryBlue.copy(alpha = 0.38f),
                            disabledContentColor = Color.White.copy(alpha = 0.78f),
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Text(
                            text = if (saving) "保存中" else "保存",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FridaDelayInputField(
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(
            color = QuickConfigDialogText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(52.dp)
                    .background(QuickConfigDialogField, RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = if (isError) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.75f)
                        } else {
                            QuickConfigDialogBorder
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = "0",
                            color = HookConfigMutedText.copy(alpha = 0.68f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    innerTextField()
                }
                Text(
                    text = "ms",
                    color = HookConfigMutedText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
        },
    )
}
