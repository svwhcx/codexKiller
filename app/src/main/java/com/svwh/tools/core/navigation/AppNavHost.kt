package com.svwh.tools.core.navigation

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.svwh.tools.core.datastore.UserSettings
import com.svwh.tools.feature.environment.presentation.EnvironmentRoute
import com.svwh.tools.feature.hookconfig.presentation.HookConfigRoute
import com.svwh.tools.feature.noenvironment.presentation.NoEnvironmentRoute
import com.svwh.tools.feature.noenvironment.presentation.repack.RepackAppListRoute
import com.svwh.tools.feature.settings.presentation.SettingsRoute
import kotlinx.coroutines.launch

private val BottomBarSelectedColor = Color(0xFF1677FF)
private val BottomBarUnselectedColor = Color(0xFF6D7788)
private val BottomBarDividerColor = Color(0xFFEFF3F8)

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
                onNavigateToHookConfig = { envType, packageName, appName ->
                    navController.navigate(
                        AppDestination.hookConfigRoute(
                            envType = envType,
                            packageName = packageName,
                            appName = appName,
                        ),
                    )
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
        composable(
            route = AppDestination.HOOK_CONFIG_ROUTE,
            arguments = listOf(
                navArgument("envType") { type = NavType.StringType },
                navArgument("packageName") { type = NavType.StringType },
                navArgument("appName") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
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
        ) { backStackEntry ->
            HookConfigRoute(
                appName = backStackEntry.arguments?.getString("appName")?.let(Uri::decode).orEmpty(),
                packageName = backStackEntry.arguments?.getString("packageName")?.let(Uri::decode).orEmpty(),
                envType = backStackEntry.arguments?.getString("envType")?.let(Uri::decode).orEmpty(),
                onBackClick = navController::popBackStack,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainTabsScreen(
    startupSettings: UserSettings,
    onNavigateToRepackAppList: () -> Unit,
    onNavigateToHookConfig: (envType: String, packageName: String, appName: String) -> Unit,
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
            SToolBottomBar(
                tabs = tabs,
                selectedIndex = pagerState.currentPage,
                onTabClick = { index ->
                    coroutineScope.launch {
                        pagerState.scrollToPage(index)
                    }
                },
            )
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
                    onNavigateToHookConfig = { packageName, appName ->
                        onNavigateToHookConfig("no_env", packageName, appName)
                    },
                )
                AppRoute.Environment -> EnvironmentRoute(
                    onNavigateToHookConfig = { packageName, appName ->
                        onNavigateToHookConfig("with_env", packageName, appName)
                    },
                )
                AppRoute.Settings -> SettingsRoute()
                else -> TabTextPage(tab = tab)
            }
        }
    }
}

@Composable
private fun SToolBottomBar(
    tabs: List<AppRoute>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            HorizontalDivider(
                color = BottomBarDividerColor,
                thickness = 0.7.dp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    SToolBottomTabItem(
                        tab = tab,
                        selected = selectedIndex == index,
                        onClick = { onTabClick(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SToolBottomTabItem(
    tab: AppRoute,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconColor by animateColorAsState(
        targetValue = if (selected) BottomBarSelectedColor else BottomBarUnselectedColor,
        label = "bottomTabIconColor",
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) BottomBarSelectedColor else Color(0xFF3F4858),
        label = "bottomTabTextColor",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(top = 6.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = tab.label,
                modifier = Modifier.size(21.dp),
                tint = iconColor,
            )
        }
        Text(
            text = tab.label,
            modifier = Modifier.padding(top = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
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
