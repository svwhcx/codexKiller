package com.svwh.tools.feature.hookconfig.domain.model

data class UserHookConfigItem(
    val id: Long,
    val packageName: String,
    val envType: String,
    val configName: String,
    val className: String,
    val methodName: String,
    val params: String,
    val type: String,
    val enabled: Boolean,
    val isLog: Boolean,
    val isInterrupted: Boolean,
    val ruleCount: Int,
    val checked: Boolean = false,
)
