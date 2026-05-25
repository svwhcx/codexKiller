package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.svwh.tools.feature.environment.presentation.HookSwitch

private val KeywordChipBackground = Color(0xFFF1F4FA)

@Composable
internal fun QuickConfigPage(
    envType: String,
    packageName: String,
    viewModel: QuickConfigViewModel = hiltViewModel(),
) {
    val groups = remember { defaultQuickConfigGroups() }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(envType, packageName) {
        viewModel.initialize(
            envType = envType,
            packageName = packageName,
            groups = groups,
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(groups, key = { it.id }) { group ->
            QuickConfigGroupCard(
                group = group,
                uiState = uiState,
                onCheckedChange = viewModel::updateQuickConfig,
            )
        }
    }
}

@Composable
private fun QuickConfigGroupCard(
    group: HookQuickConfigGroup,
    uiState: QuickConfigUiState,
    onCheckedChange: (HookQuickConfigItem, Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
        ) {
            QuickConfigGroupHeader(group = group)
            Spacer(modifier = Modifier.height(8.dp))
            group.items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = HookConfigDivider,
                    )
                }
                QuickConfigItemRow(
                    item = item,
                    checked = if (item.runtimeHookType != null) {
                        item.id in uiState.enabledItems
                    } else {
                        item.enabledByDefault
                    },
                    enabled = item.id !in uiState.loadingItems,
                    onCheckedChange = onCheckedChange,
                )
            }
        }
    }
}

@Composable
private fun QuickConfigGroupHeader(group: HookQuickConfigGroup) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(HookConfigIconContainer, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = group.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = HookConfigPrimaryBlue,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
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
    onCheckedChange: (HookQuickConfigItem, Boolean) -> Unit,
) {
    val hasDetails = item.summaryValues.isNotEmpty() || item.detailHint != null
    val isRuntimeBacked = item.runtimeHookType != null
    var localChecked by rememberSaveable(item.id) { mutableStateOf(checked) }
    val displayedChecked = if (isRuntimeBacked) checked else localChecked
    val updateChecked: (Boolean) -> Unit = { next ->
        if (isRuntimeBacked) {
            onCheckedChange(item, next)
        } else {
            localChecked = next
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasDetails && enabled) {
                updateChecked(true)
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
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
            if (hasDetails) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = HookConfigMutedText,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            HookSwitch(
                checked = displayedChecked,
                onCheckedChange = {
                    if (enabled) {
                        updateChecked(it)
                    }
                },
            )
        }

        if (item.summaryValues.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(end = 52.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item.summaryValues.take(3).forEach { value ->
                    KeywordChip(text = value)
                }
                if (item.summaryValues.size > 3) {
                    KeywordChip(text = "+${item.summaryValues.size - 3}")
                }
            }
        } else if (item.detailHint != null) {
            Text(
                text = item.detailHint,
                modifier = Modifier.padding(end = 52.dp),
                style = MaterialTheme.typography.bodySmall,
                color = HookConfigMutedText,
            )
        }
    }
}

@Composable
private fun KeywordChip(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(KeywordChipBackground)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelSmall,
        color = Color(0xFF596275),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
