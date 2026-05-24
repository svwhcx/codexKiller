package com.svwh.tools.feature.hookconfig.domain.model

data class FridaScriptItem(
    val id: Long,
    val packageName: String,
    val envType: String,
    val name: String,
    val scriptContent: String,
    val enabled: Boolean,
)
