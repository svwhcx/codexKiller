package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.svwh.tools.feature.environment.presentation.HookSwitch
import com.svwh.tools.feature.hookconfig.domain.model.HookLogRecord
import com.svwh.tools.feature.hookconfig.domain.model.HookLogTypeOption
import com.svwh.tools.ui.components.RefreshableList

private val LogSearchBackground = Color(0xFFF8FAFE)
private val LogSearchActionBackground = Color(0xFFF0F4FB)
private val LogTextPrimary = Color(0xFF2F3747)
private val LogTextSecondary = Color(0xFF6C768A)
private val LogControlCornerRadius = 10.dp
private val LogSheetScrim = Color(0x66000000)
private val LogSheetBorder = Color(0xFFE8EDF5)
private val LogSheetButtonBg = Color(0xFFF6F8FC)
private val LogSheetIconBg = Color(0xFFF8FAFF)

@Composable
internal fun HookLogPage(
    envType: String,
    packageName: String,
    viewModel: HookLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var autoScroll by rememberSaveable { mutableStateOf(true) }
    var showTypeFilterDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(envType, packageName) {
        viewModel.initialize(envType = envType, packageName = packageName)
    }

    val selectedLog = uiState.selectedLog
    if (selectedLog != null) {
        HookLogDetailPage(
            log = selectedLog,
            isLoading = uiState.isLogDetailLoading,
            onBackClick = viewModel::dismissLogDetail,
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .hookConfigGradientBackground(),
    ) {
        HookLogToolbar(
            searchQuery = uiState.searchInput,
            selectedTypeCount = uiState.selectedTypes.size,
            onSearchQueryChange = viewModel::updateSearchInput,
            onSearchClick = viewModel::submitSearch,
            autoScroll = autoScroll,
            onRefreshClick = viewModel::refresh,
            onOpenTypeFilter = { showTypeFilterDialog = true },
            onAutoScrollChange = { autoScroll = it },
        )

        if (uiState.isEmpty) {
            HookLogEmptyState(
                envType = uiState.envType,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        } else {
            RefreshableList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh,
                isLoadingMore = uiState.isLoadingMore,
                onLoadMore = viewModel::loadMore,
                hasMoreData = uiState.hasMore,
            ) {
                items(uiState.logs, key = { it.id }) { log ->
                    HookLogRow(
                        log = log,
                        onClick = { viewModel.openLogDetail(log) },
                    )
                }
            }
        }
    }

    if (showTypeFilterDialog) {
        HookLogTypeFilterBottomSheet(
            options = uiState.availableTypes,
            selectedTypes = uiState.selectedTypes,
            onDismiss = { showTypeFilterDialog = false },
            onConfirm = { selectedTypes ->
                showTypeFilterDialog = false
                viewModel.applySelectedTypes(selectedTypes)
            },
        )
    }
}

@Composable
private fun HookLogEmptyState(
    envType: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.BugReport,
            contentDescription = "暂无日志数据",
            tint = LogTextSecondary,
            modifier = Modifier.size(36.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (envType == "no_env") "暂无无环境日志数据" else "暂无有环境日志数据",
            style = MaterialTheme.typography.titleMedium,
            color = LogTextSecondary,
        )
    }
}

@Composable
private fun HookLogToolbar(
    searchQuery: String,
    selectedTypeCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    autoScroll: Boolean,
    onRefreshClick: () -> Unit,
    onOpenTypeFilter: () -> Unit,
    onAutoScrollChange: (Boolean) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HookLogSearchField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            onSearchClick = onSearchClick,
            modifier = Modifier.weight(1f),
        )

        Box {
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(LogControlCornerRadius))
                    .clickable { menuExpanded = true },
                shape = RoundedCornerShape(LogControlCornerRadius),
                color = LogSearchBackground,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "日志设置",
                        tint = LogTextSecondary,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }

            HookLogMenu(
                expanded = menuExpanded,
                autoScroll = autoScroll,
                selectedTypeCount = selectedTypeCount,
                onDismiss = { menuExpanded = false },
                onRefreshClick = {
                    onRefreshClick()
                    menuExpanded = false
                },
                onTypeFilterClick = {
                    onOpenTypeFilter()
                    menuExpanded = false
                },
                onAutoScrollChange = onAutoScrollChange,
            )
        }
    }
}

@Composable
private fun HookLogSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = LogTextPrimary),
        modifier = modifier
            .height(44.dp)
            .background(LogSearchBackground, RoundedCornerShape(LogControlCornerRadius)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 14.dp, end = 8.dp),
                ) {
                    if (value.isBlank()) {
                        Text(
                            text = "搜索日志关键字",
                            style = MaterialTheme.typography.bodyMedium,
                            color = HookConfigMutedText,
                        )
                    }
                    innerTextField()
                }
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .fillMaxHeight()
                        .background(
                            color = LogSearchActionBackground,
                            shape = RoundedCornerShape(
                                topEnd = LogControlCornerRadius,
                                bottomEnd = LogControlCornerRadius,
                            ),
                        )
                        .clip(
                            RoundedCornerShape(
                                topEnd = LogControlCornerRadius,
                                bottomEnd = LogControlCornerRadius,
                            ),
                        )
                        .clickable(onClick = onSearchClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "搜索日志",
                        tint = LogTextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
    )
}

@Composable
private fun HookLogMenu(
    expanded: Boolean,
    autoScroll: Boolean,
    selectedTypeCount: Int,
    onDismiss: () -> Unit,
    onRefreshClick: () -> Unit,
    onTypeFilterClick: () -> Unit,
    onAutoScrollChange: (Boolean) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shadowElevation = 10.dp,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier
                .width(220.dp)
                .padding(vertical = 8.dp),
        ) {
            HookLogMenuAction(
                icon = Icons.Outlined.Refresh,
                title = "刷新日志",
                onClick = onRefreshClick,
            )
            HookLogMenuAction(
                icon = Icons.Outlined.FilterList,
                title = if (selectedTypeCount > 0) "筛选日志类型 ($selectedTypeCount)" else "筛选日志类型",
                onClick = onTypeFilterClick,
            )
            HookLogMenuSwitchAction(
                icon = Icons.Outlined.CheckCircle,
                title = "自动滚动",
                checked = autoScroll,
                onCheckedChange = onAutoScrollChange,
            )
        }
    }
}

@Composable
private fun HookLogMenuAction(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LogTextPrimary,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = LogTextPrimary,
        )
    }
}

@Composable
private fun HookLogMenuSwitchAction(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LogTextPrimary,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = LogTextPrimary,
        )
        HookSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun HookLogTypeFilterBottomSheet(
    options: List<HookLogTypeOption>,
    selectedTypes: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit,
) {
    var pendingSelection by remember(selectedTypes, options) {
        mutableStateOf(selectedTypes.intersect(options.map { it.type }.toSet()))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LogSheetScrim),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
                color = Color.White,
                tonalElevation = 8.dp,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp),
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "关闭",
                                tint = LogTextPrimary,
                            )
                        }
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "筛选日志类型",
                                style = MaterialTheme.typography.titleMedium,
                                color = LogTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "选择要查看的日志类型（可多选）",
                                style = MaterialTheme.typography.bodySmall,
                                color = HookConfigMutedText,
                            )
                        }
                        TextButton(
                            onClick = { pendingSelection = emptySet() },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp),
                        ) {
                            Text(
                                text = "清空",
                                color = HookConfigPrimaryBlue,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    HorizontalDivider(color = LogSheetBorder)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                    ) {
                        items(options, key = { it.type }) { option ->
                            HookLogTypeBottomSheetRow(
                                option = option,
                                checked = option.type in pendingSelection,
                                onCheckedChange = {
                                    pendingSelection = if (option.type in pendingSelection) {
                                        pendingSelection - option.type
                                    } else {
                                        pendingSelection + option.type
                                    }
                                },
                            )
                        }
                    }

                    HorizontalDivider(color = LogSheetBorder)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "已选择 ${pendingSelection.size} 项",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LogTextSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .width(108.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LogSheetButtonBg,
                                contentColor = LogTextPrimary,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            Text(
                                text = "取消",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Button(
                            onClick = { onConfirm(pendingSelection) },
                            modifier = Modifier
                                .width(108.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HookConfigPrimaryBlue,
                                contentColor = Color.White,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            Text(
                                text = "确定",
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HookLogTypeBottomSheetRow(
    option: HookLogTypeOption,
    checked: Boolean,
    onCheckedChange: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCheckedChange)
            .height(60.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = LogSheetIconBg,
            tonalElevation = 0.dp,
            border = BorderStroke(1.dp, LogSheetBorder),
        ) {
            Icon(
                imageVector = hookLogTypeIcon(option),
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = hookLogTypeColor(option),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = option.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = LogTextPrimary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = HookConfigPrimaryBlue,
                uncheckedColor = Color(0xFFC4CAD4),
                checkmarkColor = Color.White,
            ),
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 70.dp),
        color = LogSheetBorder,
    )
}

private fun hookLogTypeIcon(option: HookLogTypeOption): ImageVector {
    return when (option.type % 5) {
        0 -> Icons.Outlined.BugReport
        1 -> Icons.Outlined.FilterList
        2 -> Icons.Outlined.Tune
        3 -> Icons.Outlined.Search
        else -> Icons.Outlined.CheckCircle
    }
}

private fun hookLogTypeColor(option: HookLogTypeOption): Color {
    return when (option.type % 5) {
        0 -> Color(0xFF2F6DF6)
        1 -> Color(0xFF24B86C)
        2 -> Color(0xFFFF7A2F)
        3 -> Color(0xFF22B8C7)
        else -> Color(0xFF7D5DF6)
    }
}

@Composable
private fun HookLogRow(
    log: HookLogRecord,
    onClick: () -> Unit,
) {
    val levelColor = when {
        log.status != null && log.status != 0 -> Color(0xFFE84C55)
        log.typeLabel.contains("文件") -> Color(0xFF37C878)
        log.typeLabel.contains("弹窗") -> Color(0xFFFF8A2A)
        else -> Color(0xFF4D73E6)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(levelColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.BugReport,
                contentDescription = null,
                tint = levelColor,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = log.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LogTextPrimary,
                    fontWeight = if (log.isRead) FontWeight.Medium else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = log.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = LogTextSecondary,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                HookLogMetaText(label = "类型：", value = log.typeLabel)
                HookLogMetaText(label = "包名：", value = log.packageName)
            }
            if (log.content.isNotBlank()) {
                Text(
                    text = log.content.replace('\n', ' '),
                    style = MaterialTheme.typography.bodySmall,
                    color = LogTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp, end = 16.dp),
        color = HookConfigDivider,
        thickness = 0.7.dp,
    )
}

@Composable
private fun HookLogDetailPage(
    log: HookLogRecord,
    isLoading: Boolean,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .hookConfigGradientBackground(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "返回",
                    tint = LogTextPrimary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "日志详情",
                    style = MaterialTheme.typography.titleMedium,
                    color = LogTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = log.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = LogTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(20.dp),
                    strokeWidth = 2.dp,
                    color = HookConfigPrimaryBlue,
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    HookLogDetailSection(title = "基础信息") {
                        HookLogDetailField("标题", log.title)
                        HookLogDetailField("类型", log.typeLabel)
                        HookLogDetailField("时间", log.time)
                        HookLogDetailField("包名", log.packageName)
                        HookLogDetailField("状态", if (log.status != null && log.status != 0) "失败(${log.status})" else "正常")
                    }

                    val parsedFields = remember(log.content) { parseHookLogFields(log.content) }
                    if (parsedFields.isNotEmpty()) {
                        HookLogDetailSection(title = "调用详情") {
                            parsedFields.forEach { field ->
                                HookLogDetailField(
                                    label = normalizeHookLogFieldLabel(field.label),
                                    value = field.value,
                                )
                            }
                        }
                    }

                    HookLogDetailTextSection(
                        title = "完整内容",
                        text = log.content.ifBlank { "暂无内容" },
                    )

                    if (!log.exp.isNullOrBlank()) {
                        HookLogDetailTextSection(
                            title = "扩展信息",
                            text = log.exp,
                        )
                    }

                    if (log.stackTrace.isNotBlank()) {
                        HookLogDetailTextSection(
                            title = "调用堆栈",
                            text = log.stackTrace,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun HookLogDetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = LogTextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content,
        )
    }
}

@Composable
private fun HookLogDetailField(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = HookConfigMutedText,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = value.ifBlank { "-" },
            style = MaterialTheme.typography.bodyMedium,
            color = LogTextPrimary,
        )
    }
}

@Composable
private fun HookLogDetailTextSection(
    title: String,
    text: String,
) {
    HookLogDetailSection(title = title) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = LogTextPrimary,
            fontFamily = FontFamily.Monospace,
        )
    }
}

private data class HookLogField(
    val label: String,
    val value: String,
)

private fun parseHookLogFields(content: String): List<HookLogField> {
    return content
        .lineSequence()
        .mapNotNull { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank()) return@mapNotNull null
            val index = listOf(
                trimmed.indexOf('：'),
                trimmed.indexOf(':'),
            ).filter { it > 0 }.minOrNull() ?: return@mapNotNull null
            val label = trimmed.substring(0, index).trim()
            val value = trimmed.substring(index + 1).trim()
            if (label.isBlank()) null else HookLogField(label, value)
        }
        .toList()
}

private fun normalizeHookLogFieldLabel(label: String): String {
    return when (label) {
        "类名", "class", "className" -> "Class"
        "方法名", "method", "methodName" -> "方法名"
        "返回值类型", "returnType" -> "返回值类型"
        "返回值", "returnValue" -> "返回值"
        "替换值", "replacement", "replacementValue", "target" -> "替换值"
        "algorithm" -> "摘要算法"
        "transformation" -> "加解密算法"
        "provider" -> "Provider"
        "mode" -> "模式"
        "key" -> "密钥"
        "params", "参数签名" -> "参数/签名"
        "inputHex" -> "输入 Hex"
        "inputTextPreview" -> "输入预览"
        "outputHex" -> "输出 Hex"
        "outputTextPreview" -> "输出预览"
        "errorType" -> "错误类型"
        "errorMessage" -> "错误原因"
        else -> label
    }
}

@Composable
private fun HookLogMetaText(
    label: String,
    value: String,
) {
    Text(
        text = "$label$value",
        style = MaterialTheme.typography.bodySmall,
        color = LogTextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
