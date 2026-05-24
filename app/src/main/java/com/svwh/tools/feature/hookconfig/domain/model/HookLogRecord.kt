package com.svwh.tools.feature.hookconfig.domain.model

data class HookLogRecord(
    val id: Long,
    val title: String,
    val time: String,
    val type: Int,
    val typeLabel: String,
    val packageName: String,
    val content: String,
    val stackTrace: String,
    val status: Int?,
    val exp: String?,
    val isRead: Boolean,
)
