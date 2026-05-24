package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
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
                    HookLogRow(log = log)
                }
            }
        }
    }

    if (showTypeFilterDialog) {
        HookLogTypeFilterDialog(
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
private fun HookLogTypeFilterDialog(
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
        ),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
            ) {
                Text(
                    text = "筛选日志类型",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = LogTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "参考旧版日志页逻辑，这里展示完整 Hook 类型列表，不依赖当前是否已有日志",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = HookConfigMutedText,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(18.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                ) {
                    items(options, key = { it.type }) { option ->
                        HookLogTypeFilterRow(
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

                HorizontalDivider(color = HookConfigDivider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "取消")
                    }
                    TextButton(
                        onClick = { pendingSelection = emptySet() },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "清空")
                    }
                    TextButton(
                        onClick = { onConfirm(pendingSelection) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(text = "确定")
                    }
                }
            }
        }
    }
}

@Composable
private fun HookLogTypeFilterRow(
    option: HookLogTypeOption,
    checked: Boolean,
    onCheckedChange: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCheckedChange)
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = option.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = LogTextPrimary,
        )
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
        )
    }
}

@Composable
private fun HookLogRow(log: HookLogRecord) {
    val levelColor = when {
        log.status != null && log.status != 0 -> Color(0xFFE84C55)
        log.typeLabel.contains("文件") -> Color(0xFF37C878)
        log.typeLabel.contains("弹窗") -> Color(0xFFFF8A2A)
        else -> Color(0xFF4D73E6)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp),
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
        modifier = Modifier.padding(start = 72.dp),
        color = HookConfigDivider,
    )
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
