package com.svwh.tools.feature.noenvironment.domain.model

import android.graphics.drawable.Drawable

data class RepackProgressState(
    val visible: Boolean = false,
    val sessionId: Long = 0L,
    val packageName: String = "",
    val appName: String = "",
    val appIcon: Drawable? = null,
    val steps: List<RepackStep> = emptyList(),
    val sessionFinished: Boolean = false,
    val outputApkPath: String = "",
    val errorDetail: String = "",
)
