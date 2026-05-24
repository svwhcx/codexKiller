package com.svwh.tools.feature.hookconfig.domain.model

data class HookLogQuery(
    val envType: String,
    val packageName: String,
    val keyword: String = "",
    val selectedTypes: Set<Int> = emptySet(),
    val pageSize: Int = 100,
    val offset: Int = 0,
)
