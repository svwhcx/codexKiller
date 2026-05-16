package com.svwh.tools.core.permission

import android.content.Context
import android.content.pm.PackageManager
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
}
