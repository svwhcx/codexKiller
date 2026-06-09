package com.svwh.tools.feature.noenvironment.data.repack

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.svwh.tools.core.navigation.AppDestination
import com.svwh.tools.MainActivity
import com.svwh.tools.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepackCompletionNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun notifyResult(
        packageName: String,
        appName: String,
        success: Boolean,
        message: String,
        detail: String? = null,
    ) {
        ensureChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentTitle(
                if (success) {
                    "重打包完成 · $appName"
                } else {
                    "重打包失败 · $appName"
                },
            )
            .setContentText(message)
            .setStyle(
                detail?.takeIf { it.isNotBlank() }?.let {
                    NotificationCompat.BigTextStyle()
                        .bigText("$message\n\n$it")
                } ?: NotificationCompat.BigTextStyle().bigText(message),
            )
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setContentIntent(createOpenAppIntent())
            .build()

        val notificationManager = context.getSystemService<NotificationManager>() ?: return
        notificationManager.notify(
            NOTIFICATION_ID_BASE + packageName.hashCode().absoluteValue(),
            notification,
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val notificationManager = context.getSystemService<NotificationManager>() ?: return
        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "重打包完成通知",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "显示重打包任务完成结果"
                enableVibration(true)
            },
        )
    }

    private fun createOpenAppIntent(): PendingIntent {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(AppDestination.EXTRA_NAV_DESTINATION, AppDestination.REPACK_APP_LIST)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentImmutableFlag(),
        )
    }

    private fun pendingIntentImmutableFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    private fun Int.absoluteValue(): Int {
        return if (this == Int.MIN_VALUE) 0 else kotlin.math.abs(this)
    }

    private companion object {
        const val CHANNEL_ID = "no_env_repack_result"
        const val NOTIFICATION_ID_BASE = 24020
    }
}
