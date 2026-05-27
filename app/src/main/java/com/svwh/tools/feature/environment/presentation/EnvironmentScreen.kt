package com.svwh.tools.feature.environment.presentation

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svwh.tools.R
import com.svwh.tools.core.permission.rememberExternalStoragePermissionGate
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem

private val SuccessGreen = Color(0xFF2E7D32)
private val SuccessContainer = Color(0xFFE7F5E9)
private val WarningRed = Color(0xFFC62828)
private val WarningContainerStart = Color(0xFFFFF0F1)
private val WarningContainerEnd = Color(0xFFFFE1E4)
internal val NoEnvironmentGreen = Color(0xFF1B7F5A)
internal val NoEnvironmentContainerStart = Color(0xFFE8F7EF)
internal val NoEnvironmentContainerEnd = Color(0xFFDDF3E8)
internal val SearchContainer = Color(0xFFF4F6FA)
internal val ListContainer = Color(0xFFFFFFFF)
internal val SwitchTrackOff = Color(0xFFE3E5E9)
internal val SwitchThumb = Color(0xFFFFFFFF)

internal val AppListControlHeight = 38.dp
internal val AppIconSize = 36.dp
internal val AppRowHorizontalPadding = 8.dp
internal val AppRowVerticalPadding = 6.dp
internal val AppListItemSpacing = 6.dp
private val BannerHorizontalPadding = 14.dp
private val BannerVerticalPadding = 10.dp
private val BannerIconSize = 20.dp
private val HookSwitchWidth = 44.dp
private val HookSwitchHeight = 26.dp
private val HookSwitchThumbSize = 22.dp
private val EnvironmentPageBackgroundTop = Color(0xFFF7FAFF)
private val EnvironmentPageBackgroundBottom = Color(0xFFFFFFFF)
private val EnvironmentTitleColor = Color(0xFF14213A)
private val EnvironmentSecondaryText = Color(0xFF7A8598)
private val EnvironmentSearchBorder = Color(0xFFE7ECF5)
private val EnvironmentPrimaryBlue = Color(0xFF1677FF)
private val EnvironmentDividerColor = Color(0xFFEFF2F7)

@Composable
fun EnvironmentRoute(
    onNavigateToHookConfig: (packageName: String, appName: String) -> Unit,
    viewModel: EnvironmentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val storagePermissionGate = rememberExternalStoragePermissionGate()

    EnvironmentScreen(
        uiState = uiState,
        onSearchQueryChange = viewModel::setSearchQuery,
        onShowSystemAppsChange = viewModel::setShowSystemApps,
        onHookEnabledChange = { packageName, enabled ->
            storagePermissionGate.runAfterPermission {
                viewModel.setHookEnabled(packageName, enabled)
            }
        },
        onAppClick = onNavigateToHookConfig,
    )
}

@Composable
private fun EnvironmentScreen(
    uiState: EnvironmentUiState,
    onSearchQueryChange: (String) -> Unit,
    onShowSystemAppsChange: (Boolean) -> Unit,
    onHookEnabledChange: (String, Boolean) -> Unit,
    onAppClick: (packageName: String, appName: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(EnvironmentPageBackgroundTop, EnvironmentPageBackgroundBottom),
                ),
            ),
    ) {
        EnvironmentHero(
            lsposedEnabled = uiState.lsposedEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(174.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            EnvironmentSearchMenuBar(
                value = uiState.searchQuery,
                showSystemApps = uiState.showSystemApps,
                onValueChange = onSearchQueryChange,
                onShowSystemAppsChange = onShowSystemAppsChange,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(22.dp))

            EnvironmentListHeader(appCount = uiState.filteredApps.size)

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                InlineLoadingRow(modifier = Modifier.fillMaxWidth())
            } else if (uiState.filteredApps.isEmpty()) {
                EnvironmentEmptyApps(modifier = Modifier.weight(1f))
            } else {
                EnvironmentAppList(
                    apps = uiState.filteredApps,
                    onHookEnabledChange = onHookEnabledChange,
                    onAppClick = onAppClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun EnvironmentHero(
    lsposedEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val subtitle = if (lsposedEnabled) {
        "为应用提供实时、独立的 Hook 运行环境"
    } else {
        "未检测到 LSPosed，当前功能可能不可用"
    }
    val subtitleColor = if (lsposedEnabled) EnvironmentSecondaryText else WarningRed

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
                .padding(start = 16.dp, top = 48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "有环境",
                style = MaterialTheme.typography.titleLarge,
                color = EnvironmentTitleColor,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = subtitleColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EnvironmentSearchMenuBar(
    value: String,
    showSystemApps: Boolean,
    onValueChange: (String) -> Unit,
    onShowSystemAppsChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filterExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, EnvironmentSearchBorder),
        shadowElevation = 4.dp,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = Color(0xFF8B97AA),
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = EnvironmentTitleColor,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isBlank()) {
                            Text(
                                text = "搜索应用名称或包名",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8B97AA),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            Box {
                Surface(
                    modifier = Modifier
                        .height(34.dp)
                        .clickable { filterExpanded = true },
                    shape = RoundedCornerShape(17.dp),
                    color = Color(0xFFF6F9FF),
                    shadowElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            tint = EnvironmentPrimaryBlue,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "菜单",
                            style = MaterialTheme.typography.bodySmall,
                            color = EnvironmentPrimaryBlue,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("仅用户应用") },
                        onClick = {
                            filterExpanded = false
                            onShowSystemAppsChange(false)
                        },
                        trailingIcon = {
                            if (!showSystemApps) {
                                Text(
                                    text = "✓",
                                    color = EnvironmentPrimaryBlue,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("包含系统应用") },
                        onClick = {
                            filterExpanded = false
                            onShowSystemAppsChange(true)
                        },
                        trailingIcon = {
                            if (showSystemApps) {
                                Text(
                                    text = "✓",
                                    color = EnvironmentPrimaryBlue,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EnvironmentListHeader(appCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "应用列表",
            style = MaterialTheme.typography.titleSmall,
            color = EnvironmentTitleColor,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "共 ${appCount} 个应用",
            style = MaterialTheme.typography.bodySmall,
            color = EnvironmentSecondaryText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 1.dp),
        )
    }
}

@Composable
private fun EnvironmentAppList(
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
            EnvironmentAppRow(
                app = app,
                onHookEnabledChange = onHookEnabledChange,
                onClick = { onAppClick(app.packageName, app.appName) },
            )
            if (index != apps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 52.dp),
                    color = EnvironmentDividerColor,
                    thickness = 0.7.dp,
                )
            }
        }
    }
}

@Composable
private fun EnvironmentAppRow(
    app: InstalledAppItem,
    onHookEnabledChange: (String, Boolean) -> Unit,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
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
                color = EnvironmentTitleColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = EnvironmentSecondaryText,
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
private fun EnvironmentEmptyApps(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "暂无有环境应用",
            style = MaterialTheme.typography.bodyMedium,
            color = EnvironmentSecondaryText,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun EnvironmentAppListCard(
    uiState: EnvironmentUiState,
    onSearchQueryChange: (String) -> Unit,
    onShowSystemAppsChange: (Boolean) -> Unit,
    onHookEnabledChange: (String, Boolean) -> Unit,
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
            EnvironmentControls(
                searchQuery = uiState.searchQuery,
                showSystemApps = uiState.showSystemApps,
                appCount = uiState.filteredApps.size,
                onSearchQueryChange = onSearchQueryChange,
                onShowSystemAppsChange = onShowSystemAppsChange,
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
private fun LsposedStatusCard(
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (enabled) SuccessGreen else WarningRed
    val message = if (enabled) {
        "当前已检测到 LSPosed 激活状态，可以执行 Hook 分析。"
    } else {
        "当前未检测到 LSPosed 激活状态，功能将不可用"
    }

    val background = if (enabled) {
        Brush.linearGradient(listOf(SuccessContainer, Color(0xFFF4FFF6)))
    } else {
        Brush.linearGradient(listOf(WarningContainerStart, WarningContainerEnd))
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .background(background)
                .padding(horizontal = BannerHorizontalPadding, vertical = BannerVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(BannerIconSize),
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
internal fun StaticInfoBanner(
    message: String,
    contentColor: Color,
    startColor: Color,
    endColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(startColor, endColor)))
                .padding(horizontal = BannerHorizontalPadding, vertical = BannerVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(BannerIconSize),
            )
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EnvironmentControls(
    searchQuery: String,
    showSystemApps: Boolean,
    appCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onShowSystemAppsChange: (Boolean) -> Unit,
) {
    var filterExpanded by remember { mutableStateOf(false) }

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

            Box {
                Surface(
                    modifier = Modifier
                        .height(AppListControlHeight)
                        .clickable { filterExpanded = true },
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "筛选",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = { filterExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("仅用户应用") },
                        onClick = {
                            filterExpanded = false
                            onShowSystemAppsChange(false)
                        },
                        trailingIcon = {
                            if (!showSystemApps) {
                                Text(
                                    text = "✓",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("包含系统应用") },
                        onClick = {
                            filterExpanded = false
                            onShowSystemAppsChange(true)
                        },
                        trailingIcon = {
                            if (showSystemApps) {
                                Text(
                                    text = "✓",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        },
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

@Composable
internal fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = modifier
            .height(AppListControlHeight)
            .background(SearchContainer, RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text(
                            text = "搜索应用名称或包名",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
internal fun AppHookRow(
    app: InstalledAppItem,
    onHookEnabledChange: (String, Boolean) -> Unit,
    onClick: (() -> Unit)? = null,
) {
    val rowModifier = if (onClick == null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    }

    Surface(
        modifier = rowModifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppRowHorizontalPadding,
                    vertical = AppRowVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InstalledAppIcon(app = app)
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (app.hookEnabled) SuccessGreen else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            HookSwitch(
                checked = app.hookEnabled,
                onCheckedChange = { enabled -> onHookEnabledChange(app.packageName, enabled) },
            )
        }
    }
}

@Composable
internal fun InstalledAppIcon(app: InstalledAppItem) {
    val icon = remember(app.packageName, app.icon) {
        app.icon.toBitmap(width = 72, height = 72).asImageBitmap()
    }

    Image(
        bitmap = icon,
        contentDescription = app.appName,
        modifier = Modifier.size(AppIconSize),
    )
}

@Composable
internal fun HookSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val trackColor = if (checked) SuccessGreen else SwitchTrackOff
    val thumbOffset = if (checked) {
        HookSwitchWidth - HookSwitchThumbSize - 2.dp
    } else {
        2.dp
    }

    Box(
        modifier = Modifier
            .size(width = HookSwitchWidth, height = HookSwitchHeight)
            .clip(RoundedCornerShape(HookSwitchHeight / 2))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(HookSwitchThumbSize)
                .clip(RoundedCornerShape(HookSwitchThumbSize / 2))
                .background(SwitchThumb),
        )
    }
}

@Composable
internal fun InlineLoadingRow(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp,
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "正在加载应用",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
