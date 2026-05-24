package com.svwh.tools.feature.hookconfig.domain.model

data class UserHookConfigDraft(
    val id: Long = 0,
    val packageName: String = "",
    val envType: String = "",
    val configName: String = "",
    val className: String = "",
    val methodName: String = "",
    val params: String = "",
    val methodSignature: String = "",
    val invokeClass: ByteArray = byteArrayOf(),
    val hookStatus: Boolean = true,
    val isLog: Boolean = true,
    val isInterrupted: Boolean = false,
    val enabled: Boolean = true,
    val exp: String = "",
    val type: String = "",
    val rules: List<UserHookConfigRule> = emptyList(),
)
