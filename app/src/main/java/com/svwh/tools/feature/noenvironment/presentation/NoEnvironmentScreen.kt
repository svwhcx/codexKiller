package com.svwh.tools.feature.noenvironment.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svwh.tools.feature.environment.presentation.AppHookRow
import com.svwh.tools.feature.environment.presentation.InlineLoadingRow
import com.svwh.tools.feature.environment.presentation.ListContainer
import com.svwh.tools.feature.environment.presentation.NoEnvironmentContainerEnd
import com.svwh.tools.feature.environment.presentation.NoEnvironmentContainerStart
import com.svwh.tools.feature.environment.presentation.NoEnvironmentGreen
import com.svwh.tools.feature.environment.presentation.SearchField

@Composable
fun NoEnvironmentRoute(
    viewModel: NoEnvironmentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NoEnvironmentScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::setSearchQuery,
        onHookEnabledChange = viewModel::setHookEnabled,
        onAddClick = {},
    )
}

@Composable
private fun NoEnvironmentScreen(
    uiState: NoEnvironmentUiState,
    onSearchQueryChange: (String) -> Unit,
    onHookEnabledChange: (String, Boolean) -> Unit,
    onAddClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            NoEnvironmentInfoBanner()
        }

        item {
            NoEnvironmentAppListCard(
                uiState = uiState,
                onSearchQueryChange = onSearchQueryChange,
                onHookEnabledChange = onHookEnabledChange,
                onAddClick = onAddClick,
            )
        }
    }
}

@Composable
private fun NoEnvironmentInfoBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = androidx.compose.ui.graphics.Color.Transparent,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(NoEnvironmentContainerStart, NoEnvironmentContainerEnd),
                    ),
                )
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = NoEnvironmentGreen,
                modifier = Modifier.size(26.dp),
            )
            Text(
                text = "无环境可重打包 App 后执行有环境相似功能。",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = NoEnvironmentGreen,
            )
        }
    }
}

@Composable
private fun NoEnvironmentAppListCard(
    uiState: NoEnvironmentUiState,
    onSearchQueryChange: (String) -> Unit,
    onHookEnabledChange: (String, Boolean) -> Unit,
    onAddClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = ListContainer,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NoEnvironmentControls(
                searchQuery = uiState.searchQuery,
                appCount = uiState.filteredApps.size,
                onSearchQueryChange = onSearchQueryChange,
                onAddClick = onAddClick,
            )

            if (uiState.isLoading) {
                InlineLoadingRow()
            } else {
                uiState.filteredApps.forEach { app ->
                    AppHookRow(
                        app = app,
                        onHookEnabledChange = onHookEnabledChange,
                    )
                }
                if (uiState.filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "没有匹配的应用",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoEnvironmentControls(
    searchQuery: String,
    appCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onAddClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SearchField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
            )

            Surface(
                modifier = Modifier
                    .height(48.dp)
                    .clickable(onClick = onAddClick),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "添加",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "应用列表",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "共 ${appCount} 个应用",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
