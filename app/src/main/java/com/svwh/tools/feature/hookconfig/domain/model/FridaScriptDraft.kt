package com.svwh.tools.feature.hookconfig.domain.model

data class FridaScriptDraft(
    val id: Long = 0,
    val packageName: String = "",
    val envType: String = "",
    val name: String = "",
    val scriptContent: String = "",
    val enabled: Boolean = true,
)
