package com.svwh.tools.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.svwh.tools.core.datastore.UserSettings
import com.svwh.tools.feature.environment.presentation.EnvironmentRoute
import com.svwh.tools.feature.noenvironment.presentation.NoEnvironmentRoute
import com.svwh.tools.feature.noenvironment.presentation.repack.RepackAppListRoute
import com.svwh.tools.feature.settings.presentation.SettingsRoute
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    startupSettings: UserSettings,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.MAIN,
        modifier = modifier,
    ) {
        composable(AppDestination.MAIN) {
            MainTabsScreen(
                startupSettings = startupSettings,
                onNavigateToRepackAppList = {
                    navController.navigate(AppDestination.REPACK_APP_LIST)
                },
            )
        }
        composable(
            route = AppDestination.REPACK_APP_LIST,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> fullWidth },
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> -fullWidth / 3 },
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { fullWidth -> fullWidth },
                )
            },
        ) {
            RepackAppListRoute()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainTabsScreen(
    startupSettings: UserSettings,
    onNavigateToRepackAppList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember(startupSettings) {
        AppRoute.bottomTabs(
            showNoEnvironment = startupSettings.showNoEnvironmentTab,
            showEnvironment = startupSettings.showEnvironmentTab,
        )
    }
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                            )
                        },
                        label = {
                            Text(text = tab.label)
                        },
                    )
                }
            }
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            beyondViewportPageCount = 1,
        ) { page ->
            when (val tab = tabs[page]) {
                AppRoute.NoEnvironment -> NoEnvironmentRoute(
                    onNavigateToRepackAppList = onNavigateToRepackAppList,
                )
                AppRoute.Environment -> EnvironmentRoute()
                AppRoute.Settings -> SettingsRoute()
                else -> TabTextPage(tab = tab)
            }
        }
    }
}

@Composable
private fun TabTextPage(tab: AppRoute) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = tab.label,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "${tab.label}页面内容占位",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
