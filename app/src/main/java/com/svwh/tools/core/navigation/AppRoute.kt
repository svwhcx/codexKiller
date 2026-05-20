package com.svwh.tools.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppRoute(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    data object Home : AppRoute(
        route = "home",
        label = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    )

    data object NoEnvironment : AppRoute(
        route = "no_environment",
        label = "无环境",
        selectedIcon = Icons.Filled.Build,
        unselectedIcon = Icons.Outlined.Build,
    )

    data object Environment : AppRoute(
        route = "environment",
        label = "有环境",
        selectedIcon = Icons.Filled.VerifiedUser,
        unselectedIcon = Icons.Outlined.VerifiedUser,
    )

    data object Settings : AppRoute(
        route = "settings",
        label = "设置",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
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
