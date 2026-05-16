package com.svwh.tools.core.navigation

sealed class AppRoute(val route: String) {
    data object Home : AppRoute("home")
    data object Settings : AppRoute("settings")
}
