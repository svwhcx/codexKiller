package com.svwh.tools.feature.hookconfig.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal enum class HookConfigTab(val title: String) {
    Quick("快捷配置"),
    User("用户配置"),
    Frida("Frida"),
    Log("日志"),
}

internal data class HookQuickConfigGroup(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val items: List<HookQuickConfigItem>,
)

internal data class HookQuickConfigItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val enabledByDefault: Boolean = false,
    val detailHint: String? = null,
    val summaryValues: List<String> = emptyList(),
)

internal data class HookLogItem(
    val id: String,
    val level: HookLogLevel,
    val title: String,
    val className: String,
    val targetLabel: String,
    val target: String,
    val timestampMillis: Long,
)

internal enum class HookLogLevel(val color: Color) {
    Info(Color(0xFF4D73E6)),
    Warn(Color(0xFFFF8A2A)),
    Debug(Color(0xFF37C878)),
    Error(Color(0xFFE84C55)),
}

internal fun defaultQuickConfigGroups(): List<HookQuickConfigGroup> {
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

internal fun sampleHookLogs(): List<HookLogItem> {
    val now = System.currentTimeMillis()
    val minute = 60_000L
    val yesterday = now - 24L * 60L * minute
    val twoDaysAgo = now - 48L * 60L * minute
    val old = now - 7L * 24L * 60L * minute

    return listOf(
        HookLogItem("1", HookLogLevel.Info, "控件赋值", "com.apicloud.a.i.a.ag.a", "控件类型：", "com.apicl...", now - minute),
        HookLogItem("2", HookLogLevel.Info, "控件赋值", "com.apicloud.a.i.a.ag.a", "控件类型：", "com.apicl...", now - 2 * minute),
        HookLogItem("3", HookLogLevel.Warn, "弹窗显示", "com.updimap.pkg.uczore.f.d", "弹窗内容：", "服务...", now - 12 * minute),
        HookLogItem("4", HookLogLevel.Info, "控件赋值", "com.apicloud.a.i.a.y.a", "控件类型：", "com.apicl...", now - 25 * minute),
        HookLogItem("5", HookLogLevel.Debug, "文件打开监听", "com.uzmap.pkg.io.FileBridge", "路径：", "/sdcard/...", now - 36 * minute),
        HookLogItem("6", HookLogLevel.Error, "控件赋值", "android.widget.TextView", "控件类型：", "android...", now - 47 * minute),
        HookLogItem("7", HookLogLevel.Info, "onClick 监听", "com.apicloud.a.i.a.ag.a", "方法：", "onClick(View)", yesterday - 22 * minute),
        HookLogItem("8", HookLogLevel.Debug, "弹窗显示", "com.uzmap.pkg.uczore.f.d", "弹窗内容：", "权限申...", twoDaysAgo - 8 * minute),
        HookLogItem("9", HookLogLevel.Warn, "网络请求监听", "okhttp3.RealCall", "URL：", "https://...", old),
    )
}

internal fun formatHookLogTime(timestampMillis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        timeInMillis = timestampMillis
    }
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    return when {
        isSameDay(now, target) -> timeFormat.format(target.time)
        isDaysBefore(now, target, 1) -> "昨天 ${timeFormat.format(target.time)}"
        isDaysBefore(now, target, 2) -> "前天 ${timeFormat.format(target.time)}"
        else -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(target.time)
    }
}

private fun isSameDay(first: Calendar, second: Calendar): Boolean {
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

private fun isDaysBefore(now: Calendar, target: Calendar, days: Int): Boolean {
    val expected = now.clone() as Calendar
    expected.add(Calendar.DAY_OF_YEAR, -days)
    return isSameDay(expected, target)
}
