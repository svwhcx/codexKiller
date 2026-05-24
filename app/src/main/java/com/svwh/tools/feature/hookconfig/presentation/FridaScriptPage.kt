package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptDraft
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem

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

private const val TEXT_EMPTY_FRIDA = "\u6682\u65e0 Frida \u811a\u672c"
private const val TEXT_LOADING = "\u52a0\u8f7d\u4e2d..."
private const val TEXT_ADD_FRIDA = "\u6dfb\u52a0\u811a\u672c"
private const val TEXT_SELECT_ALL = "\u5168\u9009"
private const val TEXT_REVERSE_SELECT = "\u53cd\u9009"
private const val TEXT_DELETE = "\u5220\u9664"
private const val TEXT_CANCEL = "\u53d6\u6d88"
private const val TEXT_BACK = "\u8fd4\u56de"
private const val TEXT_SAVE = "\u4fdd\u5b58"
private const val TEXT_SAVING = "\u4fdd\u5b58\u4e2d..."
private const val TEXT_SCRIPT_NAME_PLACEHOLDER = "\u8bf7\u8f93\u5165\u811a\u672c\u540d\u79f0"
private const val TEXT_SCRIPT_CONTENT_PLACEHOLDER = "\u811a\u672c\u5185\u5bb9\u5148\u6682\u65f6\u4f5c\u4e3a\u666e\u901a\u6587\u672c\u4fdd\u5b58"
private const val TEXT_SCRIPT_CONTENT_LABEL = "\u811a\u672c\u5185\u5bb9"
private val FridaSwitchWidth = 46.dp
private val FridaSwitchHeight = 27.dp
private val FridaSwitchThumbSize = 23.dp

@Composable
internal fun FridaScriptPage(
    envType: String,
    packageName: String,
    onNavigateToEditor: (scriptId: Long) -> Unit,
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
            .background(FridaPageBg),
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
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(uiState.items, key = { _, item -> item.id }) { _, item ->
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
                    FridaSelectionActionButton(TEXT_SELECT_ALL, viewModel::selectAll, Modifier.weight(1f))
                    FridaSelectionActionButton(TEXT_REVERSE_SELECT, viewModel::reverseSelection, Modifier.weight(1f))
                    FridaSelectionActionButton(
                        text = TEXT_DELETE,
                        onClick = viewModel::deleteSelected,
                        modifier = Modifier.weight(1f),
                        contentColor = FridaErrorText,
                    )
                    FridaSelectionActionButton(TEXT_CANCEL, viewModel::cancelSelection, Modifier.weight(1f))
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
                        contentDescription = TEXT_ADD_FRIDA,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = TEXT_ADD_FRIDA,
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = FridaShadow,
                spotColor = FridaShadow,
            ),
        shape = RoundedCornerShape(8.dp),
        color = FridaCardBg,
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
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FridaTitle,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.scriptContent.ifBlank { TEXT_SCRIPT_CONTENT_PLACEHOLDER },
                    style = MaterialTheme.typography.bodySmall,
                    color = FridaSubtitle,
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
            .background(FridaEditorBg),
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
                    FridaField(
                        value = draft.name,
                        icon = Icons.Outlined.Title,
                        placeholder = TEXT_SCRIPT_NAME_PLACEHOLDER,
                        minHeight = 42.dp,
                        singleLine = true,
                        onValueChange = { value -> onDraftChange { it.copy(name = value) } },
                    )
                }
                item {
                    Text(
                        text = TEXT_SCRIPT_CONTENT_LABEL,
                        modifier = Modifier.padding(start = 2.dp, top = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = FridaTitle,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                item {
                    FridaCodeField(
                        value = draft.scriptContent,
                        onValueChange = { value -> onDraftChange { it.copy(scriptContent = value) } },
                    )
                }
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
private fun FridaCodeField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            color = FridaCodeText,
            fontSize = 13.sp,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = FridaShadow,
                spotColor = FridaShadow,
            )
            .background(FridaCodeBg, RoundedCornerShape(14.dp))
            .padding(12.dp),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (value.isEmpty()) {
                    Text(
                        text = TEXT_SCRIPT_CONTENT_PLACEHOLDER,
                        color = Color(0xFF8F98A8),
                        fontSize = 13.sp,
                    )
                }
                innerTextField()
            }
        },
    )
}
