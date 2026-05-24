package com.svwh.tools.feature.hookconfig.domain.model

data class UserHookConfigRule(
    val id: Long = 0,
    val hookConfigId: Long = 0,
    val rule: String = "all",
    val paramNumber: Int = 0,
    val matchValue: String = "",
    val replaceValue: String = "",
    val isExpanded: Boolean = false,
)
