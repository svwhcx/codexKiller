package com.svwh.tools

import android.os.Bundle
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svwh.tools.core.datastore.UserSettings
import com.svwh.tools.core.navigation.AppDestination
import com.svwh.tools.core.designsystem.theme.SToolTheme
import com.svwh.tools.core.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var startDestination by mutableStateOf(AppDestination.MAIN)

    override fun onCreate(savedInstanceState: Bundle?) {
        startDestination = resolveStartDestination(intent)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        setContent {
            val settings by viewModel.userSettings.collectAsStateWithLifecycle()
            val themeSettings = settings ?: UserSettings()

            SToolTheme(
                themeMode = themeSettings.themeMode,
                dynamicColor = themeSettings.dynamicColor,
            ) {
                settings?.let { loadedSettings ->
                    AppNavHost(
                        startupSettings = loadedSettings,
                        startDestination = startDestination,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        startDestination = resolveStartDestination(intent)
    }

    private fun resolveStartDestination(intent: android.content.Intent?): String {
        return intent?.getStringExtra(AppDestination.EXTRA_NAV_DESTINATION)
            ?.takeIf { it.isNotBlank() }
            ?: AppDestination.MAIN
    }
}
