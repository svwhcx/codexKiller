package com.svwh.tools.feature.environment.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem
import com.svwh.tools.feature.environment.domain.repository.InstalledAppRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class InstalledAppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : InstalledAppRepository {
    override suspend fun getInstalledApps(
        showSystemApps: Boolean,
        hookedPackages: Set<String>,
    ): List<InstalledAppItem> {
        val packageManager = context.packageManager
        val currentPackageName = context.packageName

        return getInstalledApplications(packageManager)
            .asSequence()
            .filter { appInfo -> appInfo.packageName != currentPackageName }
            .mapNotNull { appInfo ->
                val isSystemApp = appInfo.isSystemApp()
                if (!showSystemApps && isSystemApp) return@mapNotNull null

                val packageInfo = runCatching {
                    getPackageInfo(packageManager, appInfo.packageName)
                }.getOrNull() ?: return@mapNotNull null

                InstalledAppItem(
                    appName = appInfo.loadLabel(packageManager).toString(),
                    packageName = appInfo.packageName,
                    icon = appInfo.loadIcon(packageManager),
                    firstInstallTimeMillis = packageInfo.firstInstallTime,
                    isSystemApp = isSystemApp,
                    hookEnabled = appInfo.packageName in hookedPackages,
                )
            }
            .sortedWith(
                compareByDescending<InstalledAppItem> { it.hookEnabled }
                    .thenByDescending { it.firstInstallTimeMillis },
            )
            .toList()
    }

    private fun getInstalledApplications(packageManager: PackageManager): List<ApplicationInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(0)
        }
    }

    private fun getPackageInfo(
        packageManager: PackageManager,
        packageName: String,
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(0),
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, 0)
    }

    private fun ApplicationInfo.isSystemApp(): Boolean {
        return flags and ApplicationInfo.FLAG_SYSTEM != 0 ||
            flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
    }
}
