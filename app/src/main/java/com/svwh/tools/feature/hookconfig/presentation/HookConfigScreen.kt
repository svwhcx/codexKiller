package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

internal val HookConfigPageBackground = Color(0xFFF7F8FC)
internal val HookConfigPrimaryBlue = Color(0xFF4668D9)
internal val HookConfigIconContainer = Color(0xFFEAF0FF)
internal val HookConfigDivider = Color(0xFFE9EDF4)
internal val HookConfigMutedText = Color(0xFF8A93A3)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HookConfigRoute(
    appName: String,
    packageName: String,
    envType: String,
    onBackClick: () -> Unit,
    onNavigateToUserConfigEditor: (envType: String, packageName: String, configId: Long, appName: String) -> Unit,
    onNavigateToFridaScriptEditor: (envType: String, packageName: String, scriptId: Long, appName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember {
        listOf(
            HookConfigTab.Quick,
            HookConfigTab.User,
            HookConfigTab.Frida,
            HookConfigTab.Log,
        )
    }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val displayName = appName.ifBlank { packageName }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = HookConfigPageBackground,
        topBar = {
            HookConfigHeader(
                appName = displayName,
                packageName = packageName,
                envType = envType,
                tabs = tabs,
                selectedIndex = pagerState.currentPage,
                onBackClick = onBackClick,
                onTabClick = { index ->
                    coroutineScope.launch {
                        pagerState.scrollToPage(index)
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
            ) { page ->
                when (tabs[page]) {
                    HookConfigTab.Quick -> QuickConfigPage()
                    HookConfigTab.User -> UserHookConfigPage(
                        envType = envType,
                        packageName = packageName,
                        appName = displayName,
                        onNavigateToEditor = { configId ->
                            onNavigateToUserConfigEditor(
                                envType,
                                packageName,
                                configId,
                                displayName,
                            )
                        },
                    )
                    HookConfigTab.Frida -> FridaScriptPage(
                        envType = envType,
                        packageName = packageName,
                        onNavigateToEditor = { scriptId ->
                            onNavigateToFridaScriptEditor(
                                envType,
                                packageName,
                                scriptId,
                                displayName,
                            )
                        },
                    )
                    HookConfigTab.Log -> HookLogPage(
                        envType = envType,
                        packageName = packageName,
                    )
                }
            }
        }
    }
}

@Composable
private fun HookConfigHeader(
    appName: String,
    packageName: String,
    envType: String,
    tabs: List<HookConfigTab>,
    selectedIndex: Int,
    onBackClick: () -> Unit,
    onTabClick: (Int) -> Unit,
) {
    Surface(
        color = Color.White,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 10.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = appName,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = packageName,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EnvironmentChip(envType = envType)
                    var menuExpanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "更多操作",
                                tint = Color(0xFF303746),
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            containerColor = Color.White,
                            shadowElevation = 10.dp,
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            DropdownMenuItem(
                                text = { Text("启动") },
                                onClick = { menuExpanded = false },
                                leadingIcon = { Icon(Icons.Outlined.PlayArrow, contentDescription = null) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            )
                            DropdownMenuItem(
                                text = { Text("重启") },
                                onClick = { menuExpanded = false },
                                leadingIcon = { Icon(Icons.Outlined.Refresh, contentDescription = null) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            )
                            DropdownMenuItem(
                                text = { Text("导出") },
                                onClick = { menuExpanded = false },
                                leadingIcon = { Icon(Icons.Outlined.Upload, contentDescription = null) },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }

            HookConfigTabs(
                tabs = tabs,
                selectedIndex = selectedIndex,
                onTabClick = onTabClick,
            )
        }
    }
}

@Composable
private fun EnvironmentChip(envType: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = HookConfigPrimaryBlue.copy(alpha = 0.10f),
    ) {
        Text(
            text = if (envType == "no_env") "无环境" else "有环境",
            modifier = Modifier
                .widthIn(min = 44.dp)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = HookConfigPrimaryBlue,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun HookConfigTabs(
    tabs: List<HookConfigTab>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clickable { onTabClick(index) },
            ) {
                Text(
                    text = tab.title,
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) HookConfigPrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(width = 42.dp, height = 2.dp)
                        .background(
                            color = if (selected) HookConfigPrimaryBlue else Color.Transparent,
                            shape = RoundedCornerShape(1.dp),
                        ),
                )
            }
        }
    }
}
