package com.svwh.tools.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.svwh.tools.feature.home.presentation.HomeRoute
import com.svwh.tools.feature.settings.presentation.SettingsRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier,
    ) {
        composable(AppRoute.Home.route) {
            HomeRoute(
                onOpenSettings = { navController.navigate(AppRoute.Settings.route) },
            )
        }
        composable(AppRoute.Settings.route) {
            SettingsRoute(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
