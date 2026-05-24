package com.svwh.tools.feature.hookconfig.domain.model

data class HookLogPageResult(
    val items: List<HookLogRecord>,
    val hasMore: Boolean,
)
