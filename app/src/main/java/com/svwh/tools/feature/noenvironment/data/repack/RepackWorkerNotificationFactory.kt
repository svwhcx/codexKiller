package com.svwh.tools.feature.noenvironment.data.repack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import com.svwh.tools.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepackWorkerNotificationFactory @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun createForegroundInfo(
        workId: UUID,
        appName: String,
        content: String,
        progress: Int,
        indeterminate: Boolean,
        ongoing: Boolean = true,
    ): ForegroundInfo {
        ensureChannel()
        val cancelIntent = WorkManager.getInstance(context).createCancelPendingIntent(workId)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("正在重打包 ${appName.ifBlank { "APK" }}")
            .setContentText(content)
            .setOnlyAlertOnce(true)
            .setOngoing(ongoing)
            .setProgress(100, progress.coerceIn(0, 100), indeterminate)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "停止",
                cancelIntent,
            )
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService<NotificationManager>() ?: return
        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "重打包进度",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "显示无环境 APK 重打包进度"
            },
        )
    }

    private companion object {
        const val CHANNEL_ID = "no_env_repack"
        const val NOTIFICATION_ID = 24010
    }
}
