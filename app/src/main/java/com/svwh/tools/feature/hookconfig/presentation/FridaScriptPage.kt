package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.launch
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptDraft
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem
import com.svwh.tools.feature.hookconfig.domain.model.GlobalFridaScriptScope

private val FridaPageBg = Color(0xFFF6F8FD)
private val FridaCardBg = Color.White
private val FridaShadow = Color(0x140F172A)
private val FridaTitle = Color(0xFF202939)
private val FridaSubtitle = Color(0xFF8B95A5)
private val FridaSwitchOff = Color(0xFFE3E8F1)
private val FridaBottomBg = Color(0xFBFFFFFF)
private val FridaEditorBg = Color(0xFFF7F9FF)
private val FridaFieldHint = Color(0xFF9AA3B2)
private val FridaErrorBg = Color(0xFFFFF0F1)
private val FridaErrorText = Color(0xFFC62828)
private val FridaCodeBg = Color(0xFF202634)
private val FridaCodeText = Color(0xFFE8EDF7)
private val FridaCodeGutterBg = Color(0xFF1A202C)
private val FridaCodeGutterText = Color(0xFF778195)
private val FridaCodeToolbarBg = Color(0xFFEDF3FF)
private val FridaCodeSuggestionBg = Color(0xFF2B3445)
private val FridaCodeScrollbarTrack = Color(0x334B5563)
private val FridaCodeScrollbarThumb = Color(0xB4CBD5E1)

private const val TEXT_EMPTY_FRIDA = "暂无 Frida 脚本"
private const val TEXT_LOADING = "加载中..."
private const val TEXT_ADD_FRIDA = "添加脚本"
private const val TEXT_IMPORT_FRIDA = "导入脚本"
private const val TEXT_IMPORT_TITLE = "从全局脚本库导入"
private const val TEXT_IMPORT_EMPTY = "暂无全局 Frida 脚本"
private const val TEXT_SELECT_ALL = "全选"
private const val TEXT_REVERSE_SELECT = "反选"
private const val TEXT_DELETE = "删除"
private const val TEXT_CANCEL = "取消"
private const val TEXT_BACK = "返回"
private const val TEXT_SAVE = "保存"
private const val TEXT_SAVING = "保存中..."
private const val TEXT_SCRIPT_NAME_PLACEHOLDER = "请输入脚本名称"
private const val TEXT_SCRIPT_EMPTY_PREVIEW = "未填写脚本内容"
private const val TEXT_SCRIPT_CONTENT_LABEL = "脚本内容"
private const val TEXT_FORMAT = "格式化"
private val FridaSwitchWidth = 46.dp
private val FridaSwitchHeight = 27.dp
private val FridaSwitchThumbSize = 23.dp
private val FridaListItemMinHeight = 74.dp
private val FridaListActionSlotSize = 48.dp
private val FridaEditorHorizontalScrollbarHeight = 12.dp
private val FridaSuggestionRowHeight = 36.dp
private val FridaSuggestionPopupVerticalPadding = 8.dp
private val FridaKeywordSuggestions = listOf(
    "Java",
    "Java.perform",
    "Java.use",
    "Interceptor",
    "Interceptor.attach",
    "Module",
    "Module.findExportByName",
    "Memory",
    "send",
    "console.log",
    "function",
    "const",
    "let",
    "var",
    "return",
    "if",
    "else",
    "for",
    "while",
    "true",
    "false",
    "null",
)

@Composable
fun GlobalFridaScriptRoute(
    onBackClick: () -> Unit,
    onNavigateToEditor: (scriptId: Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .hookConfigGradientBackground(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 14.dp, top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = TEXT_BACK)
            }
            Text(
                text = GlobalFridaScriptScope.APP_NAME,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.size(48.dp))
        }
        FridaScriptPage(
            envType = GlobalFridaScriptScope.ENV_TYPE,
            packageName = GlobalFridaScriptScope.PACKAGE_NAME,
            onNavigateToEditor = onNavigateToEditor,
            showImportAction = false,
        )
    }
}

@Composable
internal fun FridaScriptPage(
    envType: String,
    packageName: String,
    onNavigateToEditor: (scriptId: Long) -> Unit,
    showImportAction: Boolean = true,
    viewModel: FridaScriptViewModel = hiltViewModel(),
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
                viewModel.loadScripts()
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
            .hookConfigGradientBackground(),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
        ) {
            if (uiState.isLoading && uiState.items.isEmpty()) {
                Text(
                    text = TEXT_LOADING,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HookConfigMutedText,
                )
            } else if (!uiState.errorMessage.isNullOrBlank() && uiState.items.isEmpty()) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = FridaErrorText,
                    textAlign = TextAlign.Center,
                )
            } else if (uiState.items.isEmpty()) {
                EmptyFridaState(
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 6.dp),
                ) {
                    itemsIndexed(uiState.items, key = { _, item -> item.id }) { index, item ->
                        FridaScriptListItem(
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
                        if (index != uiState.items.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                                color = HookConfigDivider,
                                thickness = 0.7.dp,
                            )
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FridaBottomBg,
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
                    FridaSelectionActionButton(
                        text = TEXT_SELECT_ALL,
                        icon = Icons.Outlined.SelectAll,
                        onClick = viewModel::selectAll,
                        modifier = Modifier.weight(1f),
                    )
                    FridaSelectionActionButton(
                        text = TEXT_REVERSE_SELECT,
                        icon = Icons.Outlined.DoneAll,
                        onClick = viewModel::reverseSelection,
                        modifier = Modifier.weight(1f),
                    )
                    FridaSelectionActionButton(
                        text = TEXT_DELETE,
                        icon = Icons.Outlined.DeleteOutline,
                        onClick = viewModel::deleteSelected,
                        modifier = Modifier.weight(1f),
                        contentColor = FridaErrorText,
                    )
                    FridaSelectionActionButton(
                        text = TEXT_CANCEL,
                        icon = Icons.Outlined.Close,
                        onClick = viewModel::cancelSelection,
                        modifier = Modifier.weight(1f),
                        filled = true,
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FridaBottomActionButton(
                        text = TEXT_ADD_FRIDA,
                        icon = Icons.Default.Add,
                        onClick = { onNavigateToEditor(0L) },
                        modifier = Modifier.weight(1f),
                    )
                    if (showImportAction) {
                        FridaBottomActionButton(
                            text = TEXT_IMPORT_FRIDA,
                            icon = Icons.Outlined.FileDownload,
                            onClick = viewModel::openImportDialog,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }

    if (uiState.showImportDialog) {
        FridaImportDialog(
            items = uiState.globalItems,
            onDismiss = viewModel::closeImportDialog,
            onImport = viewModel::importGlobalScript,
        )
    }
}

@Composable
internal fun FridaScriptEditorRoute(
    envType: String,
    packageName: String,
    appName: String,
    scriptId: Long,
    onBackClick: () -> Unit,
    viewModel: FridaScriptViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(envType, packageName) {
        viewModel.initialize(envType = envType, packageName = packageName)
    }

    LaunchedEffect(scriptId) {
        if (scriptId > 0) {
            viewModel.startEdit(scriptId)
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
    FullScreenFridaScriptEditor(
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
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FridaScriptListItem(
    item: FridaScriptItem,
    checked: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckedChange: () -> Unit,
    onEnabledChange: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FridaListItemMinHeight)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                color = FridaTitle,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.scriptContent.ifBlank { TEXT_SCRIPT_EMPTY_PREVIEW },
                style = MaterialTheme.typography.bodySmall,
                color = FridaSubtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier.size(FridaListActionSlotSize),
            contentAlignment = Alignment.Center,
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = { onCheckedChange() },
                )
            } else {
                FridaSwitch(
                    checked = item.enabled,
                    onCheckedChange = { onEnabledChange() },
                )
            }
        }
    }
}

@Composable
private fun EmptyFridaState(
    modifier: Modifier = Modifier,
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
                imageVector = Icons.Outlined.Code,
                contentDescription = null,
                tint = HookConfigPrimaryBlue,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = TEXT_EMPTY_FRIDA,
            style = MaterialTheme.typography.titleMedium,
            color = FridaTitle,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun FridaSelectionActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentColor: Color = HookConfigPrimaryBlue,
    filled: Boolean = false,
) {
    val shape = RoundedCornerShape(6.dp)
    val containerColor = if (filled) HookConfigPrimaryBlue else Color.White
    val foregroundColor = if (filled) Color.White else contentColor
    Surface(
        modifier = modifier
            .requiredHeight(38.dp)
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = containerColor,
        border = if (filled) null else BorderStroke(1.dp, Color(0xFFE8EEF8)),
        shadowElevation = if (filled) 1.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(14.dp),
                tint = foregroundColor,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = foregroundColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FridaBottomActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.requiredHeight(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF7F9FF),
            contentColor = HookConfigPrimaryBlue,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 7.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FridaImportDialog(
    items: List<FridaScriptItem>,
    onDismiss: () -> Unit,
    onImport: (FridaScriptItem) -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = TEXT_IMPORT_TITLE,
                    modifier = Modifier.fillMaxWidth(),
                    color = FridaTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                if (items.isEmpty()) {
                    Text(
                        text = TEXT_IMPORT_EMPTY,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        color = FridaSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(items, key = { _, item -> item.id }) { _, item ->
                            FridaImportItem(
                                item = item,
                                onClick = { onImport(item) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FridaImportItem(
    item: FridaScriptItem,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = FridaPageBg,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.name,
                color = FridaTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.scriptContent.ifBlank { TEXT_SCRIPT_EMPTY_PREVIEW },
                color = FridaSubtitle,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FridaSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val thumbOffset = if (checked) {
        FridaSwitchWidth - FridaSwitchThumbSize - 2.dp
    } else {
        2.dp
    }

    Box(
        modifier = Modifier
            .size(width = FridaSwitchWidth, height = FridaSwitchHeight)
            .clip(RoundedCornerShape(FridaSwitchHeight / 2))
            .background(if (checked) HookConfigPrimaryBlue else FridaSwitchOff)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(FridaSwitchThumbSize)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

@Composable
private fun FullScreenFridaScriptEditor(
    appName: String,
    draft: FridaScriptDraft,
    isSaving: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onDraftChange: ((FridaScriptDraft) -> FridaScriptDraft) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .hookConfigGradientBackground(),
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0F5FF),
                        contentColor = HookConfigPrimaryBlue,
                        disabledContainerColor = Color(0xFFF0F5FF),
                        disabledContentColor = HookConfigPrimaryBlue.copy(alpha = 0.65f),
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
                FridaErrorBanner(
                    message = errorMessage,
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FridaField(
                    value = draft.name,
                    icon = Icons.Outlined.Title,
                    placeholder = TEXT_SCRIPT_NAME_PLACEHOLDER,
                    minHeight = 42.dp,
                    singleLine = true,
                    onValueChange = { value -> onDraftChange { it.copy(name = value) } },
                )
                Text(
                    text = TEXT_SCRIPT_CONTENT_LABEL,
                    modifier = Modifier.padding(start = 2.dp, top = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = FridaTitle,
                    fontWeight = FontWeight.SemiBold,
                )
                FridaCodeEditor(
                    value = draft.scriptContent,
                    onValueChange = { value -> onDraftChange { it.copy(scriptContent = value) } },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FridaErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = FridaErrorBg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = FridaErrorText,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = FridaErrorText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun FridaField(
    value: String,
    icon: ImageVector,
    placeholder: String,
    minHeight: Dp,
    singleLine: Boolean,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = FridaShadow,
                spotColor = FridaShadow,
            )
            .background(FridaCardBg, RoundedCornerShape(14.dp))
            .height(minHeight)
            .padding(horizontal = 12.dp),
        textStyle = TextStyle(
            textAlign = TextAlign.Start,
            color = FridaTitle,
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
                    tint = FridaFieldHint,
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
                            color = FridaFieldHint,
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
private fun FridaCodeEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editorValue by remember { mutableStateOf(TextFieldValue(value)) }
    var fontSize by remember { mutableFloatStateOf(13f) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val lines = remember(editorValue.text) { editorValue.text.split('\n').ifEmpty { listOf("") } }
    val editorMinWidth = remember(editorValue.text, fontSize) {
        val maxLineLength = lines.maxOfOrNull { it.length } ?: 0
        val estimatedWidth = (maxLineLength.coerceAtLeast(80) * fontSize * 0.62f).dp
        if (estimatedWidth < 720.dp) 720.dp else estimatedWidth
    }
    val cursorPosition = remember(editorValue) {
        cursorLineColumn(
            script = editorValue.text,
            cursor = editorValue.selection.start,
        )
    }
    val suggestionOffsetX = (cursorPosition.column * fontSize * 0.62f).dp
    val suggestionOffsetY = ((cursorPosition.line + 1) * fontSize * 1.45f).dp + 4.dp
    val verticalScrollOffset = with(density) { verticalScrollState.value.toDp() }
    val horizontalScrollOffset = with(density) { horizontalScrollState.value.toDp() }
    val popupX = (58.dp + suggestionOffsetX - horizontalScrollOffset).coerceAtLeast(54.dp)
    val suggestions = remember(editorValue) {
        buildFridaSuggestions(
            script = editorValue.text,
            cursor = editorValue.selection.start,
        )
    }

    LaunchedEffect(value) {
        if (value != editorValue.text) {
            editorValue = TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = FridaShadow,
                spotColor = FridaShadow,
            )
            .background(FridaCodeBg, RoundedCornerShape(14.dp)),
    ) {
        FridaCodeToolbar(
            fontSize = fontSize,
            onZoomOut = { fontSize = (fontSize - 1f).coerceAtLeast(11f) },
            onZoomIn = { fontSize = (fontSize + 1f).coerceAtMost(18f) },
            onFormat = {
                val formatted = formatFridaJavaScript(editorValue.text)
                editorValue = TextFieldValue(
                    text = formatted,
                    selection = TextRange(formatted.length),
                )
                onValueChange(formatted)
            },
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            val viewportHeight = if (maxHeight > FridaEditorHorizontalScrollbarHeight + 160.dp) {
                maxHeight - FridaEditorHorizontalScrollbarHeight
            } else {
                160.dp
            }
            val visibleLineCount = with(density) {
                (viewportHeight.toPx() / (fontSize.sp.toPx() * 1.45f)).toInt().coerceAtLeast(1)
            }
            val lineNumberCount = lines.size.coerceAtLeast(visibleLineCount)
            val popupHeight = (FridaSuggestionRowHeight * suggestions.size.coerceAtMost(5)) +
                (FridaSuggestionPopupVerticalPadding * 2)
            val popupMaxY = if (viewportHeight - popupHeight - 8.dp < 12.dp) {
                12.dp
            } else {
                viewportHeight - popupHeight - 8.dp
            }
            val popupY = (12.dp + suggestionOffsetY - verticalScrollOffset).coerceIn(
                minimumValue = 12.dp,
                maximumValue = popupMaxY,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(viewportHeight)
                    .verticalScroll(verticalScrollState),
            ) {
                Column(
                    modifier = Modifier
                        .width(46.dp)
                        .background(FridaCodeGutterBg)
                        .padding(top = 12.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    repeat(lineNumberCount) { index ->
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 9.dp),
                            color = FridaCodeGutterText,
                            fontSize = fontSize.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = (fontSize * 1.45f).sp,
                            textAlign = TextAlign.End,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(viewportHeight)
                        .horizontalScroll(horizontalScrollState)
                        .padding(12.dp),
                ) {
                    BasicTextField(
                        value = editorValue,
                        onValueChange = { next ->
                            val adjusted = applySmartIndentOnEnter(
                                previous = editorValue,
                                next = next,
                            )
                            editorValue = adjusted
                            onValueChange(adjusted.text)
                        },
                        textStyle = TextStyle(
                            color = FridaCodeText,
                            fontSize = fontSize.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = (fontSize * 1.45f).sp,
                        ),
                        visualTransformation = remember(FridaCodeBg) {
                            FridaJavaScriptHighlightTransformation(FridaCodeBg)
                        },
                        modifier = Modifier
                            .widthIn(min = editorMinWidth)
                            .defaultMinSize(minHeight = viewportHeight - 24.dp)
                            .fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Box {
                                innerTextField()
                            }
                        },
                    )
                }
            }

            if (suggestions.isNotEmpty()) {
                FridaSuggestionPopup(
                    suggestions = suggestions,
                    onSelect = { suggestion ->
                        val next = applySuggestion(editorValue, suggestion)
                        editorValue = next
                        onValueChange(next.text)
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = popupX, y = popupY)
                        .zIndex(2f),
                )
            }

            FridaVerticalScrollbar(
                scrollState = verticalScrollState,
                onDrag = { delta ->
                    coroutineScope.launch {
                        verticalScrollState.scrollTo(
                            (verticalScrollState.value + delta).coerceIn(0, verticalScrollState.maxValue),
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 5.dp)
                    .height(viewportHeight - 20.dp)
                    .width(6.dp),
            )
            FridaHorizontalScrollbar(
                scrollState = horizontalScrollState,
                onDrag = { delta ->
                    coroutineScope.launch {
                        horizontalScrollState.scrollTo(
                            (horizontalScrollState.value + delta).coerceIn(0, horizontalScrollState.maxValue),
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 52.dp, end = 16.dp, bottom = 4.dp)
                    .fillMaxWidth()
                    .height(6.dp),
            )
        }
    }
}

@Composable
private fun FridaCodeToolbar(
    fontSize: Float,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    onFormat: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FridaCodeToolbarBg, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "JS",
            modifier = Modifier
                .background(HookConfigPrimaryBlue, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "${fontSize.toInt()}sp",
            modifier = Modifier.weight(1f),
            color = FridaTitle,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        TextButton(
            onClick = onZoomOut,
            modifier = Modifier.requiredHeight(32.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
        ) {
            Text("A-", fontWeight = FontWeight.SemiBold)
        }
        TextButton(
            onClick = onZoomIn,
            modifier = Modifier.requiredHeight(32.dp),
            contentPadding = PaddingValues(horizontal = 10.dp),
        ) {
            Text("A+", fontWeight = FontWeight.SemiBold)
        }
        Button(
            onClick = onFormat,
            modifier = Modifier.requiredHeight(32.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HookConfigPrimaryBlue,
                contentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            Text(TEXT_FORMAT, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FridaVerticalScrollbar(
    scrollState: ScrollState,
    onDrag: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (scrollState.maxValue <= 0) return
    val progress = scrollState.value.toFloat() / scrollState.maxValue.toFloat()

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(FridaCodeScrollbarTrack)
            .pointerInput(scrollState.maxValue) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val track = size.height.toFloat().coerceAtLeast(1f)
                    val scrollDelta = (dragAmount.y / track * scrollState.maxValue).toInt()
                    onDrag(scrollDelta)
                }
            },
    ) {
        val thumbHeight = (maxHeight * 0.28f).coerceAtLeast(34.dp)
        val thumbOffset = (maxHeight - thumbHeight) * progress
        Box(
            modifier = Modifier
                .offset(y = thumbOffset)
                .fillMaxWidth()
                .height(thumbHeight)
                .clip(RoundedCornerShape(999.dp))
                .background(FridaCodeScrollbarThumb),
        )
    }
}

@Composable
private fun FridaHorizontalScrollbar(
    scrollState: ScrollState,
    onDrag: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (scrollState.maxValue <= 0) return
    val progress = scrollState.value.toFloat() / scrollState.maxValue.toFloat()

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(FridaCodeScrollbarTrack)
            .pointerInput(scrollState.maxValue) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val track = size.width.toFloat().coerceAtLeast(1f)
                    val scrollDelta = (dragAmount.x / track * scrollState.maxValue).toInt()
                    onDrag(scrollDelta)
                }
            },
    ) {
        val thumbWidth = (maxWidth * 0.32f).coerceAtLeast(42.dp)
        val thumbOffset = (maxWidth - thumbWidth) * progress
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .fillMaxHeight()
                .width(thumbWidth)
                .clip(RoundedCornerShape(999.dp))
                .background(FridaCodeScrollbarThumb),
        )
    }
}

@Composable
private fun FridaSuggestionPopup(
    suggestions: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val suggestionScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val popupHeight = (FridaSuggestionRowHeight * suggestions.size.coerceAtMost(5)) +
        (FridaSuggestionPopupVerticalPadding * 2)

    Surface(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = FridaShadow,
                spotColor = FridaShadow,
        ),
        shape = RoundedCornerShape(12.dp),
        color = FridaCodeSuggestionBg.copy(alpha = 0.96f),
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 160.dp, max = 260.dp)
                .height(popupHeight),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(popupHeight)
                    .verticalScroll(suggestionScrollState)
                    .padding(vertical = 8.dp),
            ) {
                suggestions.forEach { suggestion ->
                    FridaSuggestionRow(
                        text = suggestion,
                        onClick = { onSelect(suggestion) },
                    )
                }
            }
            FridaVerticalScrollbar(
                scrollState = suggestionScrollState,
                onDrag = { delta ->
                    coroutineScope.launch {
                        suggestionScrollState.scrollTo(
                            (suggestionScrollState.value + delta).coerceIn(0, suggestionScrollState.maxValue),
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 8.dp, end = 4.dp, bottom = 8.dp)
                    .height(popupHeight - 16.dp)
                    .width(3.dp),
            )
        }
    }
}

@Composable
private fun FridaSuggestionRow(
    text: String,
    onClick: () -> Unit,
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(FridaSuggestionRowHeight)
            .padding(horizontal = 12.dp),
        color = FridaCodeText,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        lineHeight = 18.sp,
    )
}

private fun buildFridaSuggestions(
    script: String,
    cursor: Int,
): List<String> {
    val prefix = currentIdentifierPrefix(script, cursor)
    if (prefix.length < 1) return emptyList()
    val declaredNames = extractDeclaredJavaScriptNames(script)
    return (declaredNames + FridaKeywordSuggestions)
        .distinct()
        .filter { it.startsWith(prefix, ignoreCase = true) && it != prefix }
        .take(8)
}

private data class CursorLineColumn(
    val line: Int,
    val column: Int,
)

private fun cursorLineColumn(
    script: String,
    cursor: Int,
): CursorLineColumn {
    val safeCursor = cursor.coerceIn(0, script.length)
    var line = 0
    var column = 0
    for (index in 0 until safeCursor) {
        if (script[index] == '\n') {
            line++
            column = 0
        } else {
            column++
        }
    }
    return CursorLineColumn(line = line, column = column)
}

private class FridaJavaScriptHighlightTransformation(
    backgroundColor: Color,
) : VisualTransformation {
    private val palette = FridaHighlightPalette.from(backgroundColor)

    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = highlightFridaJavaScript(text.text, palette),
            offsetMapping = OffsetMapping.Identity,
        )
    }
}

private data class FridaHighlightPalette(
    val keyword: Color,
    val fridaApi: Color,
    val string: Color,
    val number: Color,
    val comment: Color,
    val functionName: Color,
    val punctuation: Color,
) {
    companion object {
        fun from(backgroundColor: Color): FridaHighlightPalette {
            val dark = backgroundColor.luminance() < 0.5f
            return if (dark) {
                FridaHighlightPalette(
                    keyword = Color(0xFF7DD3FC),
                    fridaApi = Color(0xFFA7F3D0),
                    string = Color(0xFFFDE68A),
                    number = Color(0xFFF0ABFC),
                    comment = Color(0xFF94A3B8),
                    functionName = Color(0xFFFDBA74),
                    punctuation = Color(0xFFCBD5E1),
                )
            } else {
                FridaHighlightPalette(
                    keyword = Color(0xFF075985),
                    fridaApi = Color(0xFF047857),
                    string = Color(0xFF92400E),
                    number = Color(0xFF86198F),
                    comment = Color(0xFF64748B),
                    functionName = Color(0xFFC2410C),
                    punctuation = Color(0xFF475569),
                )
            }
        }
    }
}

private val JavaScriptKeywords = setOf(
    "async",
    "await",
    "break",
    "case",
    "catch",
    "class",
    "const",
    "continue",
    "debugger",
    "default",
    "delete",
    "do",
    "else",
    "export",
    "extends",
    "false",
    "finally",
    "for",
    "function",
    "if",
    "import",
    "in",
    "instanceof",
    "let",
    "new",
    "null",
    "return",
    "switch",
    "this",
    "throw",
    "true",
    "try",
    "typeof",
    "undefined",
    "var",
    "void",
    "while",
    "yield",
)

private val FridaApiNames = setOf(
    "Java",
    "Interceptor",
    "Module",
    "Memory",
    "NativeFunction",
    "NativeCallback",
    "Process",
    "Thread",
    "ObjC",
    "DebugSymbol",
    "ptr",
    "send",
    "recv",
    "console",
    "log",
    "perform",
    "use",
    "attach",
    "implementation",
    "findExportByName",
    "readUtf8String",
    "writeUtf8String",
)

private fun highlightFridaJavaScript(
    script: String,
    palette: FridaHighlightPalette,
): AnnotatedString {
    return buildAnnotatedString {
        append(script)

        highlightRegex(script, Regex("""//[^\n]*|/\*[\s\S]*?\*/"""), SpanStyle(color = palette.comment))
        highlightRegex(
            script,
            Regex("""(?:"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|`(?:\\.|[^`\\])*`)"""),
            SpanStyle(color = palette.string),
        )
        highlightRegex(script, Regex("""\b\d+(?:\.\d+)?\b"""), SpanStyle(color = palette.number))
        highlightRegex(
            script,
            Regex("""\bfunction\s+([A-Za-z_$][\w$]*)"""),
            SpanStyle(color = palette.functionName, fontWeight = FontWeight.SemiBold),
            groupIndex = 1,
        )
        highlightRegex(
            script,
            Regex("""\b([A-Za-z_$][\w$]*)\s*(?=\()"""),
            SpanStyle(color = palette.functionName),
            groupIndex = 1,
        )
        highlightRegex(
            script,
            Regex("""\b(${JavaScriptKeywords.joinToString("|")})\b"""),
            SpanStyle(color = palette.keyword, fontWeight = FontWeight.SemiBold),
        )
        highlightRegex(
            script,
            Regex("""\b(${FridaApiNames.joinToString("|")})\b"""),
            SpanStyle(color = palette.fridaApi, fontWeight = FontWeight.SemiBold),
        )
        highlightRegex(script, Regex("""[{}()\[\].,;:]"""), SpanStyle(color = palette.punctuation))
    }
}

private fun AnnotatedString.Builder.highlightRegex(
    source: String,
    regex: Regex,
    style: SpanStyle,
    groupIndex: Int = 0,
) {
    regex.findAll(source).forEach { match ->
        val range = match.groups[groupIndex]?.range ?: return@forEach
        addStyle(
            style = style,
            start = range.first,
            end = range.last + 1,
        )
    }
}

private fun currentIdentifierPrefix(script: String, cursor: Int): String {
    val safeCursor = cursor.coerceIn(0, script.length)
    var start = safeCursor
    while (start > 0) {
        val char = script[start - 1]
        if (char.isLetterOrDigit() || char == '_' || char == '$' || char == '.') {
            start--
        } else {
            break
        }
    }
    return script.substring(start, safeCursor)
}

private fun applySuggestion(
    value: TextFieldValue,
    suggestion: String,
): TextFieldValue {
    val cursor = value.selection.start.coerceIn(0, value.text.length)
    val prefix = currentIdentifierPrefix(value.text, cursor)
    val start = cursor - prefix.length
    val nextText = value.text.replaceRange(start, cursor, suggestion)
    val nextCursor = start + suggestion.length
    return TextFieldValue(
        text = nextText,
        selection = TextRange(nextCursor),
    )
}

private fun extractDeclaredJavaScriptNames(script: String): List<String> {
    val names = mutableListOf<String>()
    val declarationRegex = Regex("""\b(?:const|let|var|function|class)\s+([A-Za-z_$][\w$]*)""")
    val assignmentRegex = Regex("""\b([A-Za-z_$][\w$]*)\s*=""")
    val propertyAssignmentRegex = Regex("""\b([A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)+)\s*=""")
    val functionExpressionRegex = Regex("""\b(?:const|let|var)\s+([A-Za-z_$][\w$]*)\s*=\s*(?:function\b|\([^)]*\)\s*=>|[A-Za-z_$][\w$]*\s*=>)""")
    val objectPropertyRegex = Regex("""(?:^|[,{]\s*)([A-Za-z_$][\w$]*)\s*:""")
    val methodPropertyRegex = Regex("""(?:^|[,{]\s*)([A-Za-z_$][\w$]*)\s*\([^)]*\)\s*\{""")
    declarationRegex.findAll(script).forEach { match ->
        names += match.groupValues[1]
    }
    functionExpressionRegex.findAll(script).forEach { match ->
        names += match.groupValues[1]
    }
    propertyAssignmentRegex.findAll(script).forEach { match ->
        names += match.groupValues[1]
        names += match.groupValues[1].substringAfterLast('.')
    }
    assignmentRegex.findAll(script).forEach { match ->
        names += match.groupValues[1]
    }
    objectPropertyRegex.findAll(script).forEach { match ->
        names += match.groupValues[1]
    }
    methodPropertyRegex.findAll(script).forEach { match ->
        names += match.groupValues[1]
    }
    return names.distinct()
}

private fun applySmartIndentOnEnter(
    previous: TextFieldValue,
    next: TextFieldValue,
): TextFieldValue {
    val insertedNewline = next.text.length == previous.text.length + 1 &&
        next.text.getOrNull(next.selection.start - 1) == '\n'
    if (!insertedNewline) return next

    val cursor = next.selection.start.coerceIn(0, next.text.length)
    val beforeNewline = next.text.substring(0, cursor - 1)
    val currentLineStart = beforeNewline.lastIndexOf('\n').let { index ->
        if (index < 0) 0 else index + 1
    }
    val previousLine = beforeNewline.substring(currentLineStart)
    val baseIndent = previousLine.takeWhile { it == ' ' || it == '\t' }
    val extraIndent = if (previousLine.trimEnd().endsWith("{")) "  " else ""
    val indent = baseIndent + extraIndent
    if (indent.isEmpty()) return next

    val adjustedText = next.text.replaceRange(cursor, cursor, indent)
    val adjustedCursor = cursor + indent.length
    return TextFieldValue(
        text = adjustedText,
        selection = TextRange(adjustedCursor),
    )
}

private fun formatFridaJavaScript(script: String): String {
    if (script.isBlank()) return script
    val normalized = script
        .replace("\r\n", "\n")
        .replace("\r", "\n")
    val tokens = normalized
        .replace("{", "{\n")
        .replace("}", "\n}\n")
        .replace(";", ";\n")
        .lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    var indent = 0
    return tokens.joinToString("\n") { line ->
        if (line.startsWith("}")) {
            indent = (indent - 1).coerceAtLeast(0)
        }
        val formatted = "  ".repeat(indent) + line
        if (line.endsWith("{")) {
            indent++
        }
        formatted
    }
}
