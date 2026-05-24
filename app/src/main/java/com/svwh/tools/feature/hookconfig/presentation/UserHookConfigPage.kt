package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigDraft
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigItem
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigRule

private val UserConfigBlue = Color(0xFF4B6DE0)
private val UserConfigRuleBlue = Color(0xFF3535E7)
private val UserConfigDeleteRed = Color(0xFFE74C3C)
private val UserConfigInputGray = Color(0xFFF7F8FC)
private val UserConfigEditorBg = Color(0xFFF7F9FF)
private val UserConfigListBg = Color(0xFFF6F8FD)
private val UserConfigCardBg = Color(0xFFFFFFFF)
private val UserConfigSoftBorder = Color(0xFFF0F2F7)
private val UserConfigHint = Color(0xFF9AA3B2)
private val UserConfigShadow = Color(0x140F172A)
private val UserConfigDialogField = Color(0xFFF5F7FC)
private val UserConfigTitle = Color(0xFF202939)
private val UserConfigSubtitle = Color(0xFF8B95A5)
private val UserConfigSwitchOff = Color(0xFFE3E8F1)
private val UserConfigBottomBg = Color(0xFBFFFFFF)
private val UserConfigErrorBg = Color(0xFFFFF0F1)
private val UserConfigErrorText = Color(0xFFC62828)

private const val TEXT_EMPTY = "\u6682\u65e0\u914d\u7f6e"
private const val TEXT_SELECT_ALL = "\u5168\u9009"
private const val TEXT_REVERSE_SELECT = "\u53cd\u9009"
private const val TEXT_DELETE = "\u5220\u9664"
private const val TEXT_CANCEL = "\u53d6\u6d88"
private const val TEXT_ADD_CONFIG = "\u6dfb\u52a0\u914d\u7f6e"
private const val TEXT_BACK = "\u8fd4\u56de"
private const val TEXT_SAVING = "\u4fdd\u5b58\u4e2d..."
private const val TEXT_SAVE = "\u4fdd\u5b58"
private const val TEXT_CONFIG_NAME_PLACEHOLDER = "\u8bf7\u8f93\u5165\u914d\u7f6e\u540d\u79f0"
private const val TEXT_CLASS_NAME_PLACEHOLDER = "\u8bf7\u8f93\u5165\u7c7b\u540d"
private const val TEXT_METHOD_NAME_PLACEHOLDER = "\u8bf7\u8f93\u5165\u65b9\u6cd5\u540d"
private const val TEXT_PARAMS_PLACEHOLDER = "\u8bf7\u8f93\u5165\u53c2\u6570\u5217\u8868\uff0c\u7528\u82f1\u6587\u9017\u53f7\u5206\u9694"
private const val TEXT_LOG = "\u662f\u5426\u8bb0\u5f55\u65e5\u5fd7"
private const val TEXT_INTERRUPT = "\u662f\u5426\u62e6\u622a"
private const val TEXT_ADD = "\u6dfb\u52a0"
private const val TEXT_RETURN_VALUE = "\u8fd4\u56de\u503c"
private const val TEXT_RULE_DIALOG_TITLE = "\u4fee\u6539\u53c2\u6570\u6216\u8fd4\u56de\u503c"
private const val TEXT_MATCH_VALUE_PLACEHOLDER = "\u8bf7\u8f93\u5165\u5339\u914d\u503c"
private const val TEXT_REPLACE_VALUE_PREFIX = "\u8bf7\u8f93\u5165\u66ff\u6362\u503c\uff0c\u53c2\u6570\u53c2\u8003\uff1a"
private const val TEXT_NO_PARAMS = "\u65e0\u53c2\u6570"
private const val TEXT_REPLACE_VALUE_PLACEHOLDER = "\u8bf7\u8f93\u5165\u66ff\u6362\u503c"
private const val TEXT_EDIT_VALUE = "\u4fee\u6539\u503c"
private const val TEXT_MATCH_VALUE = "\u5339\u914d\u503c"
private const val TEXT_MATCH_ALL = "\u5168\u90e8\u66ff\u6362"
private const val TEXT_EQUALS = "\u76f8\u7b49"
private const val TEXT_CONTAINS = "\u5305\u542b"
private const val TEXT_STARTS_WITH = "\u5f00\u59cb"
private const val TEXT_ENDS_WITH = "\u7ed3\u5c3e"
private val RuleValueFieldHeight = 40.dp
private val RuleHeaderActionHeight = 34.dp
private val UserConfigSwitchWidth = 46.dp
private val UserConfigSwitchHeight = 27.dp
private val UserConfigSwitchThumbSize = 23.dp

private data class RuleOption(
    val label: String,
    val value: String,
)

private data class ParamOption(
    val label: String,
    val value: Int,
)

private val MatchTypeOptions = listOf(
    RuleOption(label = TEXT_MATCH_ALL, value = "all"),
    RuleOption(label = TEXT_EQUALS, value = "equals"),
    RuleOption(label = TEXT_CONTAINS, value = "contains"),
    RuleOption(label = TEXT_STARTS_WITH, value = "startsWith"),
    RuleOption(label = TEXT_ENDS_WITH, value = "endsWith"),
)

@Composable
internal fun UserHookConfigPage(
    envType: String,
    packageName: String,
    appName: String,
    onNavigateToEditor: (configId: Long) -> Unit,
    viewModel: UserHookConfigViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(envType, packageName) {
        viewModel.initialize(envType = envType, packageName = packageName)
    }

    DisposableEffect(lifecycleOwner, envType, packageName) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME &&
                uiState.envType == envType &&
                uiState.packageName == packageName
            ) {
                viewModel.loadConfigs()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UserConfigListBg),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        ) {
            if (uiState.items.isEmpty()) {
                EmptyUserConfigState(
                    modifier = Modifier.align(Alignment.Center),
                    onAddClick = { onNavigateToEditor(0L) },
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(uiState.items, key = { _, item -> item.id }) { _, item ->
                        UserConfigListItem(
                            item = item,
                            checked = item.id in uiState.selectedIds,
                            selectionMode = uiState.isSelectionMode,
                            onClick = {
                                if (uiState.isSelectionMode) {
                                    viewModel.toggleSelection(item.id)
                                } else {
                                    onNavigateToEditor(item.id)
                                }
                            },
                            onLongClick = { viewModel.enterSelectionMode(item.id) },
                            onCheckedChange = { viewModel.toggleSelection(item.id) },
                            onEnabledChange = { viewModel.toggleEnabled(item) },
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = UserConfigBottomBg,
            shadowElevation = 10.dp,
        ) {
            if (uiState.isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .height(50.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SelectionActionButton(
                        text = TEXT_SELECT_ALL,
                        onClick = viewModel::selectAll,
                        modifier = Modifier.weight(1f),
                    )
                    SelectionActionButton(
                        text = TEXT_REVERSE_SELECT,
                        onClick = viewModel::reverseSelection,
                        modifier = Modifier.weight(1f),
                    )
                    SelectionActionButton(
                        text = TEXT_DELETE,
                        onClick = viewModel::deleteSelected,
                        modifier = Modifier.weight(1f),
                        contentColor = UserConfigDeleteRed,
                    )
                    SelectionActionButton(
                        text = TEXT_CANCEL,
                        onClick = viewModel::cancelSelection,
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Button(
                    onClick = { onNavigateToEditor(0L) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .requiredHeight(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF7F9FF),
                        contentColor = HookConfigPrimaryBlue,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = TEXT_ADD_CONFIG,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = TEXT_ADD_CONFIG,
                        modifier = Modifier.padding(start = 7.dp),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
internal fun UserHookConfigEditorRoute(
    envType: String,
    packageName: String,
    appName: String,
    configId: Long,
    onBackClick: () -> Unit,
    viewModel: UserHookConfigViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(envType, packageName) {
        viewModel.initialize(envType = envType, packageName = packageName)
    }

    LaunchedEffect(configId) {
        if (configId > 0) {
            viewModel.startEdit(configId)
        } else {
            viewModel.startCreate()
        }
    }

    LaunchedEffect(uiState.saveSuccessToken) {
        if (uiState.saveSuccessToken != 0L) {
            viewModel.consumeSaveSuccess()
            onBackClick()
        }
    }

    val draft = uiState.editingDraft ?: return
    FullScreenUserConfigEditor(
        appName = appName.ifBlank { packageName },
        draft = draft,
        isSaving = uiState.isSaving,
        errorMessage = uiState.errorMessage,
        onBack = {
            viewModel.closeEditor()
            onBackClick()
        },
        onSave = viewModel::saveDraft,
        onDraftChange = viewModel::updateDraft,
        onAddRule = viewModel::addRule,
        onRemoveRule = viewModel::removeRule,
        onUpdateRule = viewModel::updateRule,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserConfigListItem(
    item: UserHookConfigItem,
    checked: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckedChange: () -> Unit,
    onEnabledChange: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = UserConfigShadow,
                spotColor = UserConfigShadow,
            ),
        shape = RoundedCornerShape(8.dp),
        color = UserConfigCardBg,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = item.configName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = UserConfigTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.className}.${item.methodName}(${item.params})",
                    style = MaterialTheme.typography.bodySmall,
                    color = UserConfigSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (selectionMode) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { onCheckedChange() },
                )
            } else {
                UserConfigSwitch(
                    checked = item.enabled,
                    onCheckedChange = { onEnabledChange() },
                )
            }
        }
    }
}

@Composable
private fun EmptyUserConfigState(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(HookConfigIconContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = HookConfigPrimaryBlue,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = TEXT_EMPTY,
            style = MaterialTheme.typography.titleMedium,
            color = UserConfigTitle,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onAddClick) {
            Text(TEXT_ADD_CONFIG, color = HookConfigPrimaryBlue)
        }
    }
}

@Composable
private fun SelectionActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = HookConfigPrimaryBlue,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.requiredHeight(38.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color(0xFFF7F9FF),
            contentColor = contentColor,
        ),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun UserConfigSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val thumbOffset = if (checked) {
        UserConfigSwitchWidth - UserConfigSwitchThumbSize - 2.dp
    } else {
        2.dp
    }

    Box(
        modifier = Modifier
            .size(width = UserConfigSwitchWidth, height = UserConfigSwitchHeight)
            .clip(RoundedCornerShape(UserConfigSwitchHeight / 2))
            .background(if (checked) HookConfigPrimaryBlue else UserConfigSwitchOff)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(UserConfigSwitchThumbSize)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
private fun FullScreenUserConfigEditor(
    appName: String,
    draft: UserHookConfigDraft,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDraftChange: ((UserHookConfigDraft) -> UserHookConfigDraft) -> Unit,
    onAddRule: (UserHookConfigRule) -> Unit,
    onRemoveRule: (Int) -> Unit,
    onUpdateRule: (Int, (UserHookConfigRule) -> UserHookConfigRule) -> Unit,
) {
    var showAddRulePanel by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UserConfigEditorBg),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 6.dp, end = 14.dp, top = 8.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = TEXT_BACK)
                }
                Text(
                    text = appName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0F5FF),
                        contentColor = UserConfigBlue,
                        disabledContainerColor = Color(0xFFF0F5FF),
                        disabledContentColor = UserConfigBlue.copy(alpha = 0.65f),
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier.requiredHeight(34.dp),
                ) {
                    Text(
                        text = if (isSaving) TEXT_SAVING else TEXT_SAVE,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                UserConfigErrorBanner(
                    message = errorMessage,
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 14.dp,
                    end = 14.dp,
                    top = 2.dp,
                    bottom = 28.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    GrayField(
                        value = draft.configName,
                        icon = Icons.Outlined.PersonOutline,
                        placeholder = TEXT_CONFIG_NAME_PLACEHOLDER,
                        onValueChange = { value -> onDraftChange { it.copy(configName = value) } },
                    )
                }
                item {
                    GrayField(
                        value = draft.className,
                        icon = Icons.Outlined.Widgets,
                        placeholder = TEXT_CLASS_NAME_PLACEHOLDER,
                        onValueChange = { value -> onDraftChange { it.copy(className = value) } },
                    )
                }
                item {
                    GrayField(
                        value = draft.methodName,
                        icon = Icons.Outlined.Build,
                        placeholder = TEXT_METHOD_NAME_PLACEHOLDER,
                        onValueChange = { value -> onDraftChange { it.copy(methodName = value) } },
                    )
                }
                item {
                    GrayField(
                        value = draft.params,
                        icon = Icons.AutoMirrored.Outlined.ListAlt,
                        placeholder = TEXT_PARAMS_PLACEHOLDER,
                        onValueChange = { value -> onDraftChange { it.copy(params = value) } },
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = draft.isLog,
                                onCheckedChange = { checked ->
                                    onDraftChange { it.copy(isLog = checked) }
                                },
                            )
                            Text(TEXT_LOG)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = draft.isInterrupted,
                                onCheckedChange = { checked ->
                                    onDraftChange { it.copy(isInterrupted = checked) }
                                },
                            )
                            Text(TEXT_INTERRUPT)
                        }
                    }
                }
                item {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = UserConfigShadow,
                                spotColor = UserConfigShadow,
                            ),
                        onClick = { showAddRulePanel = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UserConfigBlue,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = TEXT_ADD,
                            tint = Color.White,
                        )
                        Text(
                            text = TEXT_ADD,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                itemsIndexed(draft.rules) { index, rule ->
                    ChangeRuleItem(
                        title = if (rule.paramNumber < 1) TEXT_RETURN_VALUE else "\u53c2\u6570${rule.paramNumber}",
                        rule = rule,
                        onDelete = { onRemoveRule(index) },
                        onRuleChange = { transform -> onUpdateRule(index, transform) },
                    )
                }
            }
        }

        if (showAddRulePanel) {
            AddRuleOverlay(
                params = draft.params,
                onDismiss = { showAddRulePanel = false },
                onConfirm = { rule ->
                    onAddRule(rule.copy(isExpanded = false))
                    showAddRulePanel = false
                },
            )
        }
    }
}

@Composable
private fun UserConfigErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = UserConfigErrorBg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = UserConfigErrorText,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = UserConfigErrorText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun GrayField(
    value: String,
    icon: ImageVector,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = UserConfigShadow,
                spotColor = UserConfigShadow,
            )
            .background(UserConfigCardBg, RoundedCornerShape(14.dp))
            .height(40.dp)
            .padding(horizontal = 12.dp),
        textStyle = TextStyle(
            textAlign = TextAlign.Start,
            color = Color(0xFF202939),
            fontSize = 14.sp,
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = UserConfigHint,
                    modifier = Modifier.size(17.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = UserConfigHint,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun AddRuleOverlay(
    params: String,
    onDismiss: () -> Unit,
    onConfirm: (UserHookConfigRule) -> Unit,
) {
    var matchValue by remember { mutableStateOf("") }
    var replaceValue by remember { mutableStateOf("") }
    var selectedParam by remember { mutableStateOf(ParamOption(TEXT_RETURN_VALUE, 0)) }
    var selectedRuleType by remember { mutableStateOf(MatchTypeOptions.first()) }
    val paramOptions = remember(params) {
        buildList {
            add(ParamOption(TEXT_RETURN_VALUE, 0))
            params
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .forEachIndexed { index, _ ->
                    add(ParamOption("\u53c2\u6570${index + 1}", index + 1))
                }
        }
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
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = UserConfigCardBg),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 14.dp),
                ) {
                    Text(
                        text = TEXT_RULE_DIALOG_TITLE,
                        modifier = Modifier.align(Alignment.Center),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1F2937),
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SpinnerLikeDropdown(
                        modifier = Modifier.weight(1f),
                        items = paramOptions.map { it.label },
                        selectedLabel = selectedParam.label,
                        onSelect = { label ->
                            selectedParam = paramOptions.first { it.label == label }
                        },
                    )
                    SpinnerLikeDropdown(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        items = MatchTypeOptions.map { it.label },
                        selectedLabel = selectedRuleType.label,
                        onSelect = { label ->
                            selectedRuleType = MatchTypeOptions.first { it.label == label }
                        },
                    )
                }
                if (selectedRuleType.value != "all") {
                    DialogInputField(
                        value = matchValue,
                        placeholder = TEXT_MATCH_VALUE_PLACEHOLDER,
                        minHeight = 42.dp,
                        singleLine = true,
                        onValueChange = { matchValue = it },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                DialogInputField(
                    value = replaceValue,
                    placeholder = TEXT_REPLACE_VALUE_PLACEHOLDER,
                    minHeight = 54.dp,
                    singleLine = false,
                    onValueChange = { replaceValue = it },
                )
                if (params.isNotBlank()) {
                    Text(
                        text = TEXT_REPLACE_VALUE_PREFIX + params,
                        modifier = Modifier.padding(top = 6.dp, start = 2.dp),
                        color = UserConfigHint,
                        fontSize = 11.sp,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .requiredHeight(42.dp),
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UserConfigDialogField,
                            contentColor = Color(0xFF4B5563),
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Text(
                            text = TEXT_CANCEL,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .requiredHeight(42.dp),
                        onClick = {
                            onConfirm(
                                UserHookConfigRule(
                                    rule = selectedRuleType.value,
                                    matchValue = matchValue,
                                    replaceValue = replaceValue,
                                    paramNumber = selectedParam.value,
                                ),
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UserConfigBlue,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    ) {
                        Text(
                            text = TEXT_SAVE,
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
private fun DialogInputField(
    value: String,
    placeholder: String,
    minHeight: androidx.compose.ui.unit.Dp,
    singleLine: Boolean,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = TextStyle(
            color = Color(0xFF374151),
            fontSize = 14.sp,
        ),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UserConfigCardBg, RoundedCornerShape(12.dp))
                    .height(minHeight)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.TopStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = UserConfigHint,
                        fontSize = 14.sp,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun SpinnerLikeDropdown(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedLabel: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = UserConfigDialogField,
                contentColor = Color(0xFF374151),
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            onClick = { expanded = true },
        ) {
            Text(
                text = selectedLabel,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color(0xFF374151),
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(148.dp),
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                        )
                    },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ChangeRuleItem(
    title: String,
    rule: UserHookConfigRule,
    onDelete: () -> Unit,
    onRuleChange: ((UserHookConfigRule) -> UserHookConfigRule) -> Unit,
) {
    var expanded by remember(rule.id, rule.paramNumber, rule.rule, rule.isExpanded) {
        mutableStateOf(rule.isExpanded)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = UserConfigShadow,
                spotColor = UserConfigShadow,
            )
            .background(UserConfigCardBg, RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.requiredHeight(RuleHeaderActionHeight),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = UserConfigRuleBlue,
                    disabledContentColor = Color.White,
                ),
                onClick = {},
                enabled = false,
            ) {
                Text(
                    text = getMatchTypeLabel(rule.rule),
                    color = Color.White,
                    fontSize = 12.sp,
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .requiredHeight(RuleHeaderActionHeight)
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    color = Color(0xFF667085),
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF667085),
                )
            }
            TextButton(
                onClick = onDelete,
                shape = RectangleShape,
                modifier = Modifier.requiredHeight(RuleHeaderActionHeight),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = UserConfigDeleteRed,
                ),
            ) {
                Text(
                    text = TEXT_DELETE,
                    color = UserConfigDeleteRed,
                    fontSize = 13.sp,
                )
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8FAFF),
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    RuleInlineInputField(
                        value = rule.replaceValue,
                        placeholder = TEXT_EDIT_VALUE,
                        onValueChange = { value ->
                            onRuleChange { it.copy(replaceValue = value) }
                        },
                    )
                    if (rule.rule != "all") {
                        HorizontalDivider(color = UserConfigSoftBorder)
                        RuleInlineInputField(
                            value = rule.matchValue,
                            placeholder = TEXT_MATCH_VALUE,
                            onValueChange = { value ->
                                onRuleChange { it.copy(matchValue = value) }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleInlineInputField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color(0xFF7A869A),
        ),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(RuleValueFieldHeight)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF98A2B3),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            }
        },
    )
}

private fun getMatchTypeLabel(matchType: String): String {
    return when (matchType) {
        "contains" -> TEXT_CONTAINS
        "equals" -> TEXT_EQUALS
        "startsWith" -> TEXT_STARTS_WITH
        "endsWith" -> TEXT_ENDS_WITH
        else -> "\u5168\u90e8"
    }
}
