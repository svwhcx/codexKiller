package com.svwh.tools.feature.noenvironment.presentation.repack

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svwh.tools.R
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem
import com.svwh.tools.feature.environment.presentation.InlineLoadingRow
import com.svwh.tools.feature.environment.presentation.InstalledAppIcon
import com.svwh.tools.feature.noenvironment.presentation.repack.progress.RepackProgressSheetHost

private val RepackPageBackgroundTop = Color(0xFFF7FAFF)
private val RepackPageBackgroundBottom = Color(0xFFFFFFFF)
private val RepackPageAmbientBlue = Color(0xFFEAF3FF)
private val RepackTitleColor = Color(0xFF14213A)
private val RepackSecondaryText = Color(0xFF7A8598)
private val RepackSearchBorder = Color(0xFFE3EAF5)
private val RepackSearchSurface = Color(0xB8F4F8FF)
private val RepackPrimaryBlue = Color(0xFF1677FF)
private val RepackDividerColor = Color(0xFFEFF2F7)
private val RepackActionInset = Color(0xFFDCE9FA)
private val RepackActionBorder = Color(0xFFCFE0F5)
private val RepackActionHighlight = Color(0xFFF8FBFF)
private val RepackActionShadow = Color(0xFFE6F0FC)

@Composable
fun RepackAppListRoute(
    viewModel: RepackAppListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val repackProgressState by viewModel.repackProgressState.collectAsStateWithLifecycle()
    val installCoordinator = rememberRepackInstallCoordinator(
        onInstallSucceeded = viewModel::onInstallSucceeded,
    )
    var showRepackDetails by remember { mutableStateOf(false) }

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
            onDetails = { showRepackDetails = true },
        )

        if (showRepackDetails) {
            RepackFailureDetailScreen(
                state = repackProgressState,
                onClose = { showRepackDetails = false },
            )
        }
    }
}

@Composable
private fun RepackFailureDetailScreen(
    state: com.svwh.tools.feature.noenvironment.domain.model.RepackProgressState,
    onClose: () -> Unit,
) {
    val terminalMessage = state.steps.lastOrNull { it.isTerminal }?.label.orEmpty()
    val detail = state.errorDetail

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "重打包错误详情",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = state.appName.ifBlank { state.packageName },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "关闭",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clickable(onClick = onClose),
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 14.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            ) {
                Text(
                    text = buildString {
                        if (terminalMessage.isNotBlank()) {
                            appendLine(terminalMessage)
                            appendLine()
                        }
                        append(detail.ifBlank { "暂无错误详情" })
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                )
            }
        }
    }
}

@Composable
private fun RepackAppListScreen(
    uiState: RepackAppListUiState,
    onSearchQueryChange: (String) -> Unit,
    onRepackClick: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to RepackPageBackgroundTop,
                        0.32f to RepackPageAmbientBlue,
                        0.58f to Color(0xFFFAFCFF),
                        1.00f to RepackPageBackgroundBottom,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            RepackHero(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                RepackSearchBar(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(22.dp))

                RepackListHeader(
                    appCount = uiState.filteredApps.size,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.isLoading) {
                    InlineLoadingRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )
                } else if (uiState.filteredApps.isEmpty()) {
                    RepackEmptyApps(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                    )
                } else {
                    RepackAppList(
                        apps = uiState.filteredApps,
                        onRepackClick = onRepackClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RepackHero(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.ic_repackage_header),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.TopEnd,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .offset(x = (-10).dp, y = 10.dp)
                .size(width = 160.dp, height = 160.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 34.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = "重打包",
                    style = MaterialTheme.typography.titleLarge,
                    color = RepackTitleColor,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = RepackPrimaryBlue,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = "选择应用并写入无环境运行能力",
                style = MaterialTheme.typography.bodyMedium,
                color = RepackSecondaryText,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun RepackSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(8.dp),
        color = RepackSearchSurface,
        border = BorderStroke(1.dp, RepackSearchBorder),
        shadowElevation = 0.dp,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color(0xFF8996AA),
                modifier = Modifier.size(17.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = RepackTitleColor,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isBlank()) {
                            Text(
                                text = "搜索应用名称或包名",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8D98AA),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}

@Composable
private fun RepackListHeader(
    appCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "应用列表",
            style = MaterialTheme.typography.titleSmall,
            color = RepackTitleColor,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "共 ${appCount} 个应用",
            style = MaterialTheme.typography.bodySmall,
            color = RepackSecondaryText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 1.dp),
        )
    }
}

@Composable
private fun RepackAppList(
    apps: List<InstalledAppItem>,
    onRepackClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        itemsIndexed(
            items = apps,
            key = { _, app -> app.packageName },
        ) { index, app ->
            RepackAppRow(
                app = app,
                onRepackClick = onRepackClick,
            )
            if (index != apps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                    color = RepackDividerColor,
                    thickness = 0.7.dp,
                )
            }
        }
    }
}

@Composable
private fun RepackAppRow(
    app: InstalledAppItem,
    onRepackClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRepackClick(app.packageName) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InstalledAppIcon(app = app)
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = app.appName,
                style = MaterialTheme.typography.bodyLarge,
                color = RepackTitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = RepackSecondaryText,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            modifier = Modifier
                .width(58.dp)
                .height(34.dp)
                .clickable { onRepackClick(app.packageName) },
            shape = RoundedCornerShape(8.dp),
            color = RepackActionInset,
            border = BorderStroke(1.dp, RepackActionBorder),
            shadowElevation = 0.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(RepackActionShadow, RepackActionHighlight),
                        ),
                        RoundedCornerShape(8.dp),
                    )
                    .padding(horizontal = 13.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "打包",
                    style = MaterialTheme.typography.bodySmall,
                    color = RepackPrimaryBlue,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun RepackEmptyApps(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "暂无可重打包应用",
            style = MaterialTheme.typography.bodyMedium,
            color = RepackSecondaryText,
            fontWeight = FontWeight.Medium,
        )
    }
}
