package com.svwh.tools.feature.environment.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem
import com.svwh.tools.feature.environment.domain.repository.InstalledAppRepository
import com.svwh.tools.feature.environment.domain.repository.InstalledAppMetaDataFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class InstalledAppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : InstalledAppRepository {
    override suspend fun getInstalledApps(
        showSystemApps: Boolean,
        hookedPackages: Set<String>,
        requiredMetaData: InstalledAppMetaDataFilter?,
    ): List<InstalledAppItem> {
        val packageManager = context.packageManager
        val currentPackageName = context.packageName

        return getInstalledApplications(packageManager, requiredMetaData != null)
            .asSequence()
            .filter { appInfo -> appInfo.packageName != currentPackageName }
            .filter { appInfo -> appInfo.matchesMetaData(requiredMetaData) }
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

    private fun getInstalledApplications(
        packageManager: PackageManager,
        includeMetaData: Boolean,
    ): List<ApplicationInfo> {
        val flags = if (includeMetaData) PackageManager.GET_META_DATA else 0
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(flags.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(flags)
        }
    }

    private fun ApplicationInfo.matchesMetaData(filter: InstalledAppMetaDataFilter?): Boolean {
        if (filter == null) return true
        val actualValue = metaData?.get(filter.name) ?: return false
        return filter.value == null || actualValue.toString() == filter.value
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
