package com.svwh.tools.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppRoute(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : AppRoute(
        route = "home",
        label = "首页",
        icon = Icons.Outlined.Home,
    )

    data object NoEnvironment : AppRoute(
        route = "no_environment",
        label = "无环境",
        icon = Icons.Outlined.Build,
    )

    data object Environment : AppRoute(
        route = "environment",
        label = "有环境",
        icon = Icons.Outlined.CheckCircle,
    )

    data object Settings : AppRoute(
        route = "settings",
        label = "设置",
        icon = Icons.Outlined.Settings,
    )

    companion object {
        fun bottomTabs(
            showNoEnvironment: Boolean,
            showEnvironment: Boolean,
        ): List<AppRoute> {
            return buildList {
                add(Home)
                if (showNoEnvironment) add(NoEnvironment)
                if (showEnvironment) add(Environment)
                add(Settings)
            }
        }
    }
}
