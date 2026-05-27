package com.svwh.tools.feature.noenvironment.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.svwh.tools.core.permission.rememberExternalStoragePermissionGate
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem
import com.svwh.tools.feature.environment.presentation.HookSwitch
import com.svwh.tools.feature.environment.presentation.InlineLoadingRow
import com.svwh.tools.feature.environment.presentation.InstalledAppIcon

private val PageBackgroundTop = Color(0xFFF7FAFF)
private val PageBackgroundBottom = Color(0xFFFFFFFF)
private val PageAmbientBlue = Color(0xFFEAF3FF)
private val TitleColor = Color(0xFF14213A)
private val SecondaryText = Color(0xFF7A8598)
private val SearchBorder = Color(0xFFE3EAF5)
private val SearchSurface = Color(0xB8F4F8FF)
private val SearchDivider = Color(0xFFE9EEF7)
private val AddButtonSurface = Color(0xE8EAF3FF)
private val PrimaryBlue = Color(0xFF1677FF)
private val DividerColor = Color(0xFFEFF2F7)

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to PageBackgroundTop,
                        0.32f to PageAmbientBlue,
                        0.58f to Color(0xFFFAFCFF),
                        1.00f to PageBackgroundBottom,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            NoEnvironmentHero(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(174.dp),
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                NoEnvironmentSearchBar(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    onAddClick = onAddClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(22.dp))

                AppListHeader(
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
                    EmptyNoEnvironmentApps(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                    )
                } else {
                    NoEnvironmentAppList(
                        apps = uiState.filteredApps,
                        onHookEnabledChange = onHookEnabledChange,
                        onAppClick = onAppClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NoEnvironmentHero(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.bg_no_environment_header),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
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
                    text = "无环境",
                    style = MaterialTheme.typography.titleLarge,
                    color = TitleColor,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = "为应用提供纯净、独立的运行环境",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun NoEnvironmentSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(8.dp),
        color = SearchSurface,
        border = BorderStroke(1.dp, SearchBorder),
        shadowElevation = 0.dp,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp),
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
                    color = TitleColor,
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

            Box(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .width(1.dp)
                    .height(44.dp)
                    .background(SearchDivider),
            )

            Surface(
                modifier = Modifier
                    .height(44.dp)
                    .clickable(onClick = onAddClick),
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    bottomStart = 0.dp,
                    topEnd = 8.dp,
                    bottomEnd = 8.dp,
                ),
                color = AddButtonSurface,
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(17.dp),
                    )
                    Text(
                        text = "添加",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppListHeader(
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
            color = TitleColor,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "共 ${appCount} 个应用",
            style = MaterialTheme.typography.bodySmall,
            color = SecondaryText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 1.dp),
        )
    }
}

@Composable
private fun NoEnvironmentAppList(
    apps: List<InstalledAppItem>,
    onHookEnabledChange: (String, Boolean) -> Unit,
    onAppClick: (packageName: String, appName: String) -> Unit,
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
            NoEnvironmentAppRow(
                app = app,
                onHookEnabledChange = onHookEnabledChange,
                onClick = { onAppClick(app.packageName, app.appName) },
            )
            if (index != apps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                    color = DividerColor,
                    thickness = 0.7.dp,
                )
            }
        }
    }
}

@Composable
private fun NoEnvironmentAppRow(
    app: InstalledAppItem,
    onHookEnabledChange: (String, Boolean) -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                color = TitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryText,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        HookSwitch(
            checked = app.hookEnabled,
            onCheckedChange = { enabled -> onHookEnabledChange(app.packageName, enabled) },
        )
    }
}

@Composable
private fun EmptyNoEnvironmentApps(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "暂无无环境应用",
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
            fontWeight = FontWeight.Medium,
        )
    }
}
