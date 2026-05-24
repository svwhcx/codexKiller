package com.svwh.tools.feature.noenvironment.presentation.repack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.svwh.tools.feature.environment.presentation.AppListItemSpacing
import com.svwh.tools.feature.environment.presentation.InlineLoadingRow
import com.svwh.tools.feature.environment.presentation.ListContainer
import com.svwh.tools.feature.environment.presentation.SearchField
import com.svwh.tools.feature.noenvironment.presentation.components.InstalledAppRepackRow
import com.svwh.tools.feature.noenvironment.presentation.components.RepackListTopBar
import com.svwh.tools.feature.noenvironment.presentation.repack.progress.RepackProgressSheetHost

@Composable
fun RepackAppListRoute(
    viewModel: RepackAppListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val repackProgressState by viewModel.repackProgressState.collectAsStateWithLifecycle()
    val installCoordinator = rememberRepackInstallCoordinator()

    Box(modifier = Modifier.fillMaxSize()) {
        RepackAppListScreen(
            uiState = uiState,
            onSearchQueryChange = viewModel::setSearchQuery,
            onRepackClick = viewModel::onRepackClick,
        )

        RepackProgressSheetHost(
            state = repackProgressState,
            onDismiss = viewModel::dismissRepackProgress,
            onStop = viewModel::stopRepackProgress,
            onInstall = { installCoordinator.install(repackProgressState) },
            onDetails = viewModel::showRepackDetails,
        )
    }
}

@Composable
private fun RepackAppListScreen(
    uiState: RepackAppListUiState,
    onSearchQueryChange: (String) -> Unit,
    onRepackClick: (String) -> Unit,
) {
    Scaffold(
        topBar = { RepackListTopBar() },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 15.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                RepackAppListCard(
                    uiState = uiState,
                    onSearchQueryChange = onSearchQueryChange,
                    onRepackClick = onRepackClick,
                )
            }
        }
    }
}

@Composable
private fun RepackAppListCard(
    uiState: RepackAppListUiState,
    onSearchQueryChange: (String) -> Unit,
    onRepackClick: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = ListContainer,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SearchField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
            )

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
                    text = "共 ${uiState.filteredApps.size} 个应用",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (uiState.isLoading) {
                InlineLoadingRow()
            } else if (uiState.filteredApps.isEmpty()) {
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
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppListItemSpacing)) {
                    uiState.filteredApps.forEach { app ->
                        InstalledAppRepackRow(
                            app = app,
                            onItemClick = { onRepackClick(app.packageName) },
                            onRepackClick = onRepackClick,
                        )
                    }
                }
            }
        }
    }
}
