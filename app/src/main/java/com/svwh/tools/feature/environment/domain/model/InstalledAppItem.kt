package com.svwh.tools.feature.environment.domain.model

import android.graphics.drawable.Drawable

data class InstalledAppItem(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    val firstInstallTimeMillis: Long,
    val isSystemApp: Boolean,
    val hookEnabled: Boolean,
)
