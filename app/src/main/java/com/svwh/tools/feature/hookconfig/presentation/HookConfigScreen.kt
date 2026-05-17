package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.svwh.tools.feature.environment.presentation.HookSwitch
import kotlinx.coroutines.launch

private val PageBackground = Color(0xFFF7F8FC)
private val PrimaryBlue = Color(0xFF4668D9)
private val IconContainer = Color(0xFFEAF0FF)
private val CardDivider = Color(0xFFE9EDF4)
private val MutedText = Color(0xFF8A93A3)
private val KeywordChipBackground = Color(0xFFF1F4FA)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HookConfigRoute(
    appName: String,
    packageName: String,
    envType: String,
    onBackClick: () -> Unit,
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PageBackground,
        topBar = {
            HookConfigTopBar(
                appName = appName.ifBlank { packageName },
                packageName = packageName,
                envType = envType,
                onBackClick = onBackClick,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            HookConfigTabs(
                tabs = tabs,
                selectedIndex = pagerState.currentPage,
                onTabClick = { index ->
                    coroutineScope.launch {
                        pagerState.scrollToPage(index)
                    }
                },
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
            ) { page ->
                when (tabs[page]) {
                    HookConfigTab.Quick -> QuickConfigPage()
                    HookConfigTab.User -> PlaceholderConfigPage(
                        title = "用户配置",
                        message = "后续用于展示自定义 class、方法签名、参数和变量修改规则。",
                    )
                    HookConfigTab.Frida -> PlaceholderConfigPage(
                        title = "Frida",
                        message = "后续用于管理 Frida 脚本、注入参数和运行状态。",
                    )
                    HookConfigTab.Log -> PlaceholderConfigPage(
                        title = "日志",
                        message = "后续用于查看 Hook 命中记录、异常堆栈和调试输出。",
                    )
                }
            }
        }
    }
}

@Composable
private fun HookConfigTopBar(
    appName: String,
    packageName: String,
    envType: String,
    onBackClick: () -> Unit,
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 10.dp, top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = if (envType == "no_env") "无环境" else "有环境",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
            HookTopAction(icon = Icons.Outlined.Save, contentDescription = "保存")
            HookTopAction(icon = Icons.Outlined.Refresh, contentDescription = "刷新")
            HookTopAction(icon = Icons.Outlined.PlayArrow, contentDescription = "运行")
        }
    }
}

@Composable
private fun HookTopAction(
    icon: ImageVector,
    contentDescription: String,
) {
    IconButton(onClick = {}) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color(0xFF303746),
        )
    }
}

@Composable
private fun HookConfigTabs(
    tabs: List<HookConfigTab>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
) {
    Surface(color = Color.White) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable { onTabClick(index) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 42.dp, height = 2.dp)
                            .background(
                                color = if (selected) PrimaryBlue else Color.Transparent,
                                shape = RoundedCornerShape(1.dp),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickConfigPage() {
    val groups = remember { defaultQuickConfigGroups() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(groups, key = { it.id }) { group ->
            QuickConfigGroupCard(group = group)
        }
    }
}

@Composable
private fun QuickConfigGroupCard(group: HookQuickConfigGroup) {
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
                        color = CardDivider,
                    )
                }
                QuickConfigItemRow(item = item)
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
                .background(IconContainer, RoundedCornerShape(7.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = group.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = PrimaryBlue,
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
                color = MutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuickConfigItemRow(item: HookQuickConfigItem) {
    var checked by rememberSaveable(item.id) { mutableStateOf(item.enabledByDefault) }
    val hasDetails = item.summaryValues.isNotEmpty() || item.detailHint != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = hasDetails) {
                checked = true
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
                        color = MutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (hasDetails) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MutedText,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            HookSwitch(
                checked = checked,
                onCheckedChange = { checked = it },
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
                color = MutedText,
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

@Composable
private fun PlaceholderConfigPage(
    title: String,
    message: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private enum class HookConfigTab(val title: String) {
    Quick("快捷配置"),
    User("用户配置"),
    Frida("Frida"),
    Log("日志"),
}

private data class HookQuickConfigGroup(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val items: List<HookQuickConfigItem>,
)

private data class HookQuickConfigItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val enabledByDefault: Boolean = false,
    val detailHint: String? = null,
    val summaryValues: List<String> = emptyList(),
)

private fun defaultQuickConfigGroups(): List<HookQuickConfigGroup> {
    return listOf(
        HookQuickConfigGroup(
            id = "algorithm",
            title = "算法分析",
            description = "配置与算法相关的 Hook 选项",
            icon = Icons.Outlined.BarChart,
            items = listOf(
                HookQuickConfigItem("digest", "摘要算法", "MD5, SHA...", enabledByDefault = true),
                HookQuickConfigItem("cipher", "加解密算法", "AES, DES, RSA...", enabledByDefault = true),
                HookQuickConfigItem("random", "随机数监听", "SecureRandom, UUID..."),
                HookQuickConfigItem("base64", "Base64 编解码监听"),
            ),
        ),
        HookQuickConfigGroup(
            id = "network",
            title = "网络环境",
            description = "配置网络相关的 Hook 选项",
            icon = Icons.Outlined.Language,
            items = listOf(
                HookQuickConfigItem("hide_wifi_proxy", "隐藏 Wifi 代理", enabledByDefault = true),
                HookQuickConfigItem("hide_vpn", "隐藏 VPN", enabledByDefault = true),
                HookQuickConfigItem("dns_lookup", "DNS 查询监听"),
                HookQuickConfigItem("socket_connect", "Socket 连接监听"),
            ),
        ),
        HookQuickConfigGroup(
            id = "behavior",
            title = "行为记录",
            description = "配置应用行为监听相关的 Hook 选项",
            icon = Icons.Outlined.Security,
            items = listOf(
                HookQuickConfigItem("assets_read", "Assets 资源读取监听", enabledByDefault = true),
                HookQuickConfigItem("file_read", "文件读取监听", enabledByDefault = true),
                HookQuickConfigItem("file_write", "文件写入监听", enabledByDefault = true),
                HookQuickConfigItem("file_delete", "文件删除监听", enabledByDefault = true),
                HookQuickConfigItem("shell_exec", "shell 命令执行监听", enabledByDefault = true),
            ),
        ),
        HookQuickConfigGroup(
            id = "interaction",
            title = "交互监听",
            description = "配置用户交互相关的 Hook 选项",
            icon = Icons.Outlined.TouchApp,
            items = listOf(
                HookQuickConfigItem(
                    id = "onclick",
                    title = "onClick 监听",
                    subtitle = "捕获 OnClickListener 与点击入口",
                    enabledByDefault = true,
                    detailHint = "后续可配置目标 class、方法签名与变量修改策略",
                ),
                HookQuickConfigItem("long_click", "onLongClick 监听"),
                HookQuickConfigItem("touch_event", "触摸事件监听"),
            ),
        ),
        HookQuickConfigGroup(
            id = "ui",
            title = "UI",
            description = "配置界面相关的 Hook 选项",
            icon = Icons.Outlined.Widgets,
            items = listOf(
                HookQuickConfigItem("dialog_position", "弹窗定位", enabledByDefault = true),
                HookQuickConfigItem(
                    id = "dialog_block_keywords",
                    title = "屏蔽关键词弹窗（点击设置内容）",
                    enabledByDefault = true,
                    summaryValues = listOf("升级", "广告", "风险提示", "隐私授权"),
                ),
                HookQuickConfigItem("text_value", "控件文本赋值记录", enabledByDefault = true),
                HookQuickConfigItem("activity_record", "Activity 记录", enabledByDefault = true),
                HookQuickConfigItem("toast_record", "Toast 记录"),
            ),
        ),
        HookQuickConfigGroup(
            id = "storage",
            title = "存储与组件",
            description = "配置存储、Intent 与组件启动监听",
            icon = Icons.Outlined.FolderOpen,
            items = listOf(
                HookQuickConfigItem("shared_prefs", "SharedPreferences 读写监听"),
                HookQuickConfigItem("database", "SQLite 数据库访问监听"),
                HookQuickConfigItem("intent", "Intent 跳转监听"),
                HookQuickConfigItem("clipboard", "剪贴板访问监听"),
            ),
        ),
        HookQuickConfigGroup(
            id = "anti_debug",
            title = "反调试",
            description = "配置调试、模拟器与完整性检测 Hook",
            icon = Icons.Outlined.BugReport,
            items = listOf(
                HookQuickConfigItem("debugger", "调试器检测绕过"),
                HookQuickConfigItem("emulator", "模拟器特征隐藏"),
                HookQuickConfigItem("root", "Root 环境隐藏"),
                HookQuickConfigItem("signature", "签名校验监听"),
            ),
        ),
    )
}
