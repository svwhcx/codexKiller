package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.svwh.tools.feature.hookconfig.domain.model.HookLogRecord
import com.svwh.tools.feature.hookconfig.domain.model.HookLogTypeOption
import com.svwh.tools.ui.components.RefreshableList

private val FridaLogSearchBackground = Color(0xFFF8FAFE)
private val FridaLogSearchActionBackground = Color(0xFFF0F4FB)
private val FridaLogTextPrimary = Color(0xFF2F3747)
private val FridaLogTextSecondary = Color(0xFF6C768A)
private val FridaLogSheetScrim = Color(0x66000000)
private val FridaLogSheetBorder = Color(0xFFE8EDF5)
private val FridaLogSheetButtonBg = Color(0xFFF6F8FC)
private val FridaLogSheetIconBg = Color(0xFFF8FAFF)
private val FridaLogListItemMinHeight = 76.dp
private val FridaLogControlCornerRadius = 10.dp

@Composable
internal fun FridaLogPage(
    packageName: String,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: FridaLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLevelFilterDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(packageName) {
        viewModel.initialize(packageName = packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .hookConfigGradientBackground(),
    ) {
        FridaLogToolbar(
            searchQuery = uiState.searchInput,
            selectedLevelCount = uiState.selectedLevels.size,
            selectedLogCount = uiState.selectedIds.size,
            isSelectionMode = uiState.isSelectionMode,
            onSearchQueryChange = viewModel::updateSearchInput,
            onSearchClick = viewModel::submitSearch,
            onRefreshClick = viewModel::refresh,
            onOpenLevelFilter = { showLevelFilterDialog = true },
            onClearLogs = viewModel::clearLogs,
            onSelectAll = viewModel::selectAllVisible,
            onDeleteSelected = viewModel::deleteSelectedLogs,
            onCancelSelection = viewModel::clearSelection,
        )

        if (uiState.isEmpty) {
            FridaLogEmptyState(
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
                    FridaLogRow(
                        log = log,
                        checked = log.id in uiState.selectedIds,
                        selectionMode = uiState.isSelectionMode,
                        onClick = {
                            if (uiState.isSelectionMode) {
                                viewModel.toggleSelected(log.id)
                            } else {
                                onNavigateToDetail(log.id)
                            }
                        },
                        onLongClick = { viewModel.startSelection(log.id) },
                    )
                }
            }
        }
    }

    if (showLevelFilterDialog) {
        FridaLevelFilterBottomSheet(
            options = uiState.availableLevels,
            selectedLevels = uiState.selectedLevels,
            onDismiss = { showLevelFilterDialog = false },
            onConfirm = { selectedLevels ->
                showLevelFilterDialog = false
                viewModel.applySelectedLevels(selectedLevels)
            },
        )
    }
}

@Composable
private fun FridaLogToolbar(
    searchQuery: String,
    selectedLevelCount: Int,
    selectedLogCount: Int,
    isSelectionMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onRefreshClick: () -> Unit,
    onOpenLevelFilter: () -> Unit,
    onClearLogs: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCancelSelection: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (isSelectionMode) {
            Text(
                text = "已选择 $selectedLogCount 条",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = FridaLogTextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            FridaLogSearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                onSearchClick = onSearchClick,
                modifier = Modifier.weight(1f),
            )
        }

        Box {
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(FridaLogControlCornerRadius))
                    .clickable { menuExpanded = true },
                shape = RoundedCornerShape(FridaLogControlCornerRadius),
                color = FridaLogSearchBackground,
                shadowElevation = 0.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Frida日志设置",
                        tint = FridaLogTextSecondary,
                        modifier = Modifier.size(21.dp),
                    )
                }
            }

            FridaLogMenu(
                expanded = menuExpanded,
                selectedLevelCount = selectedLevelCount,
                isSelectionMode = isSelectionMode,
                onDismiss = { menuExpanded = false },
                onRefreshClick = {
                    onRefreshClick()
                    menuExpanded = false
                },
                onLevelFilterClick = {
                    onOpenLevelFilter()
                    menuExpanded = false
                },
                onClearLogs = {
                    onClearLogs()
                    menuExpanded = false
                },
                onSelectAll = {
                    onSelectAll()
                    menuExpanded = false
                },
                onDeleteSelected = {
                    onDeleteSelected()
                    menuExpanded = false
                },
                onCancelSelection = {
                    onCancelSelection()
                    menuExpanded = false
                },
            )
        }
    }
}

@Composable
private fun FridaLogSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = FridaLogTextPrimary),
        modifier = modifier
            .height(44.dp)
            .background(FridaLogSearchBackground, RoundedCornerShape(FridaLogControlCornerRadius)),
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
                            text = "搜索Frida日志",
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
                            color = FridaLogSearchActionBackground,
                            shape = RoundedCornerShape(
                                topEnd = FridaLogControlCornerRadius,
                                bottomEnd = FridaLogControlCornerRadius,
                            ),
                        )
                        .clip(
                            RoundedCornerShape(
                                topEnd = FridaLogControlCornerRadius,
                                bottomEnd = FridaLogControlCornerRadius,
                            ),
                        )
                        .clickable(onClick = onSearchClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "搜索",
                        tint = FridaLogTextSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
    )
}

@Composable
private fun FridaLogMenu(
    expanded: Boolean,
    selectedLevelCount: Int,
    isSelectionMode: Boolean,
    onDismiss: () -> Unit,
    onRefreshClick: () -> Unit,
    onLevelFilterClick: () -> Unit,
    onClearLogs: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCancelSelection: () -> Unit,
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
            if (isSelectionMode) {
                FridaMenuAction(Icons.Outlined.SelectAll, "全选当前列表", onSelectAll)
                FridaMenuAction(Icons.Outlined.Delete, "删除选中日志", onDeleteSelected)
                FridaMenuAction(Icons.Outlined.Close, "退出选择", onCancelSelection)
            } else {
                FridaMenuAction(Icons.Outlined.Refresh, "刷新日志", onRefreshClick)
                FridaMenuAction(
                    icon = Icons.Outlined.FilterList,
                    title = if (selectedLevelCount > 0) {
                        "筛选日志级别($selectedLevelCount)"
                    } else {
                        "筛选日志级别"
                    },
                    onClick = onLevelFilterClick,
                )
                FridaMenuAction(Icons.Outlined.Delete, "清空日志", onClearLogs)
            }
        }
    }
}

@Composable
private fun FridaMenuAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            tint = FridaLogTextPrimary,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = FridaLogTextPrimary,
        )
    }
}

@Composable
private fun FridaLevelFilterBottomSheet(
    options: List<HookLogTypeOption>,
    selectedLevels: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (Set<Int>) -> Unit,
) {
    var pendingSelection by remember(selectedLevels, options) {
        mutableStateOf(selectedLevels.intersect(options.map { it.type }.toSet()))
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
                .background(FridaLogSheetScrim),
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "关闭",
                                tint = FridaLogTextPrimary,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "筛选日志级别",
                                style = MaterialTheme.typography.titleMedium,
                                color = FridaLogTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "不选择时查询全部日志",
                                style = MaterialTheme.typography.bodySmall,
                                color = HookConfigMutedText,
                            )
                        }
                        TextButton(onClick = { pendingSelection = emptySet() }) {
                            Text(
                                text = "清空",
                                color = HookConfigPrimaryBlue,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    HorizontalDivider(color = FridaLogSheetBorder)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                    ) {
                        items(options, key = { it.type }) { option ->
                            FridaLevelRow(
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

                    HorizontalDivider(color = FridaLogSheetBorder)

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
                            color = FridaLogTextSecondary,
                            fontWeight = FontWeight.Medium,
                        )
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .width(108.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FridaLogSheetButtonBg,
                                contentColor = FridaLogTextPrimary,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        ) {
                            Text(text = "取消", fontWeight = FontWeight.SemiBold)
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
                            Text(text = "确定", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FridaLevelRow(
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
        FridaLevelBadge(level = option.title)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = option.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = FridaLogTextPrimary,
            fontWeight = FontWeight.Medium,
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
        color = FridaLogSheetBorder,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FridaLogRow(
    log: HookLogRecord,
    checked: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FridaLogListItemMinHeight)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .background(if (checked) HookConfigPrimaryBlue.copy(alpha = 0.08f) else Color.Transparent)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectionMode) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = HookConfigPrimaryBlue,
                    uncheckedColor = Color(0xFFC4CAD4),
                    checkmarkColor = Color.White,
                ),
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        FridaLevelBadge(level = log.typeLabel)
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
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
                    color = FridaLogTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = log.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = FridaLogTextSecondary,
                    maxLines = 1,
                    textAlign = TextAlign.End,
                )
            }
            Text(
                text = log.content.replace('\n', ' ').ifBlank { " " },
                style = MaterialTheme.typography.bodySmall,
                color = FridaLogTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(start = if (selectionMode) 114.dp else 66.dp, end = 16.dp),
        color = HookConfigDivider,
        thickness = 0.7.dp,
    )
}

@Composable
private fun FridaLevelBadge(
    level: String,
) {
    val levelColor = fridaLevelColor(level)
    Surface(
        modifier = Modifier
            .size(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = FridaLogSheetIconBg,
        tonalElevation = 0.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.BugReport,
                contentDescription = null,
                tint = levelColor,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private fun fridaLevelColor(level: String): Color {
    return when (level.uppercase()) {
        "ERROR" -> Color(0xFFE84C55)
        "WARN", "WARNING" -> Color(0xFFFF8A2A)
        else -> Color(0xFF2F6DF6)
    }
}

@Composable
private fun FridaLogEmptyState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.BugReport,
            contentDescription = "暂无Frida日志",
            tint = FridaLogTextSecondary,
            modifier = Modifier.size(36.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无Frida日志",
            style = MaterialTheme.typography.titleMedium,
            color = FridaLogTextSecondary,
        )
    }
}
