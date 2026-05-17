package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.svwh.tools.feature.environment.presentation.HookSwitch

private val LogSearchBackground = Color(0xFFF8FAFE)
private val LogSearchActionBackground = Color(0xFFF0F4FB)
private val LogTextPrimary = Color(0xFF2F3747)
private val LogTextSecondary = Color(0xFF6C768A)
private val LogControlCornerRadius = 10.dp

@Composable
internal fun HookLogPage() {
    var searchInput by rememberSaveable { mutableStateOf("") }
    var submittedSearchQuery by rememberSaveable { mutableStateOf("") }
    var errorOnly by rememberSaveable { mutableStateOf(false) }
    var autoScroll by rememberSaveable { mutableStateOf(true) }
    val logs = remember { sampleHookLogs() }
    val filteredLogs = remember(submittedSearchQuery, errorOnly, logs) {
        logs.filter { log ->
            val matchesLevel = !errorOnly || log.level == HookLogLevel.Error
            val matchesQuery = submittedSearchQuery.isBlank() ||
                log.title.contains(submittedSearchQuery, ignoreCase = true) ||
                log.className.contains(submittedSearchQuery, ignoreCase = true) ||
                log.target.contains(submittedSearchQuery, ignoreCase = true)
            matchesLevel && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        HookLogToolbar(
            searchQuery = searchInput,
            onSearchQueryChange = { searchInput = it },
            onSearchClick = { submittedSearchQuery = searchInput.trim() },
            errorOnly = errorOnly,
            autoScroll = autoScroll,
            onErrorOnlyChange = { errorOnly = it },
            onAutoScrollChange = { autoScroll = it },
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 22.dp),
        ) {
            items(filteredLogs, key = { it.id }) { log ->
                HookLogRow(log = log)
            }

            item {
                Text(
                    text = "已加载全部日志",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = HookConfigMutedText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun HookLogToolbar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    errorOnly: Boolean,
    autoScroll: Boolean,
    onErrorOnlyChange: (Boolean) -> Unit,
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
                errorOnly = errorOnly,
                autoScroll = autoScroll,
                onDismiss = { menuExpanded = false },
                onSearchClick = {
                    onSearchClick()
                    menuExpanded = false
                },
                onErrorOnlyChange = onErrorOnlyChange,
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
                            text = "搜索日志关键词",
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
    errorOnly: Boolean,
    autoScroll: Boolean,
    onDismiss: () -> Unit,
    onSearchClick: () -> Unit,
    onErrorOnlyChange: (Boolean) -> Unit,
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
                icon = Icons.Outlined.Delete,
                title = "清空日志",
                onClick = onDismiss,
            )
            HookLogMenuAction(
                icon = Icons.Outlined.Save,
                title = "导出日志",
                onClick = onDismiss,
            )
            HookLogMenuAction(
                icon = Icons.Outlined.Search,
                title = "搜索日志",
                onClick = onSearchClick,
            )
            HookLogMenuAction(
                icon = Icons.Outlined.FilterList,
                title = "仅显示 Error",
                highlightTail = "Error",
                onClick = { onErrorOnlyChange(!errorOnly) },
            )
            HookLogMenuSwitchAction(
                icon = Icons.Outlined.Refresh,
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
    highlightTail: String? = null,
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
        Row(modifier = Modifier.weight(1f)) {
            Text(
                text = title.removeSuffix(highlightTail.orEmpty()),
                style = MaterialTheme.typography.titleMedium,
                color = LogTextPrimary,
            )
            if (highlightTail != null) {
                Text(
                    text = highlightTail,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE53935),
                )
            }
        }
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
private fun HookLogRow(log: HookLogItem) {
    val levelColor = log.level.color

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
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatHookLogTime(log.timestampMillis),
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
                HookLogMetaText(label = "类名：", value = log.className)
                HookLogMetaText(label = log.targetLabel, value = log.target)
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
