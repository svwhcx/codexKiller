package com.svwh.tools.feature.noenvironment.presentation

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svwh.tools.core.permission.rememberExternalStoragePermissionGate
import com.svwh.tools.feature.environment.presentation.AppHookRow
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem
import com.svwh.tools.feature.environment.presentation.AppListControlHeight
import com.svwh.tools.feature.environment.presentation.AppListItemSpacing
import com.svwh.tools.feature.environment.presentation.InlineLoadingRow
import com.svwh.tools.feature.environment.presentation.ListContainer
import com.svwh.tools.feature.environment.presentation.NoEnvironmentContainerEnd
import com.svwh.tools.feature.environment.presentation.NoEnvironmentContainerStart
import com.svwh.tools.feature.environment.presentation.NoEnvironmentGreen
import com.svwh.tools.feature.environment.presentation.SearchField
import com.svwh.tools.feature.environment.presentation.StaticInfoBanner

import com.svwh.tools.feature.environment.presentation.StaticInfoBanner

@Composable
fun NoEnvironmentRoute(
    onNavigateToRepackAppList: () -> Unit,
    onNavigateToHookConfig: (packageName: String, appName: String) -> Unit,
    viewModel: NoEnvironmentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val storagePermissionGate = rememberExternalStoragePermissionGate()

    NoEnvironmentScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::setSearchQuery,
        onHookEnabledChange = { packageName, enabled ->
            storagePermissionGate.runAfterPermission {
                viewModel.setHookEnabled(packageName, enabled)
            }
        },
        onAddClick = onNavigateToRepackAppList,
        onAppClick = onNavigateToHookConfig,
    )
}

@Composable
private fun NoEnvironmentScreen(
    uiState: NoEnvironmentUiState,
    onSearchQueryChange: (String) -> Unit,
    onHookEnabledChange: (String, Boolean) -> Unit,
    onAddClick: () -> Unit,
    onAppClick: (packageName: String, appName: String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        NoEnvironmentInfoBanner()

        Spacer(modifier = Modifier.height(12.dp)) // Add some space between banner and card

        NoEnvironmentAppListCard(
            uiState = uiState,
            onSearchQueryChange = onSearchQueryChange,
            onHookEnabledChange = onHookEnabledChange,
            onAddClick = onAddClick,
            onAppClick = onAppClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NoEnvironmentInfoBanner() {
    StaticInfoBanner(
        message = "无环境可重打包 App 后执行有环境相似功能。",
        contentColor = NoEnvironmentGreen,
        startColor = NoEnvironmentContainerStart,
        endColor = NoEnvironmentContainerEnd,
    )
}

@Composable
private fun NoEnvironmentAppListCard(
    uiState: NoEnvironmentUiState,
    onSearchQueryChange: (String) -> Unit,
    onHookEnabledChange: (String, Boolean) -> Unit,
    onAddClick: () -> Unit,
    onAppClick: (packageName: String, appName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = ListContainer,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            NoEnvironmentControls(
                searchQuery = uiState.searchQuery,
                appCount = uiState.filteredApps.size,
                onSearchQueryChange = onSearchQueryChange,
                onAddClick = onAddClick,
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                InlineLoadingRow(modifier = Modifier.weight(1f))
            } else {
                if (uiState.filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .height(160.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "没有匹配的应用",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(AppListItemSpacing),
                    ) {
                        items(items = uiState.filteredApps, key = { appItem: InstalledAppItem -> appItem.packageName }) { app: InstalledAppItem ->
                            AppHookRow(
                                app = app,
                                onHookEnabledChange = onHookEnabledChange,
                                onClick = { onAppClick(app.packageName, app.appName) },
                            )
                        }
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
                    .height(AppListControlHeight)
                    .clickable(onClick = onAddClick),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "添加",
                        style = MaterialTheme.typography.bodySmall,
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
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "共 ${appCount} 个应用",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
