package com.svwh.tools.core.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getStatus(permission: String): PermissionStatus {
        val granted = ContextCompat.checkSelfPermission(
            context,
            permission,
        ) == PackageManager.PERMISSION_GRANTED

        return if (granted) PermissionStatus.Granted else PermissionStatus.Denied
    }

    fun getPackageVisibilityAccess(): PackageVisibilityAccess {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return PackageVisibilityAccess.LegacyNoRuntimePermission
        }

        val requestedPermissions = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_PERMISSIONS,
                )
            }.requestedPermissions.orEmpty()
        }.getOrDefault(emptyArray())

        return if (Manifest.permission.QUERY_ALL_PACKAGES in requestedPermissions) {
            PackageVisibilityAccess.QueryAllPackagesDeclared
        } else {
            PackageVisibilityAccess.LimitedVisibility
        }
    }
}
