package com.svwh.tools.feature.hookconfig.domain.repository

import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.HookLogPageResult
import com.svwh.tools.feature.hookconfig.domain.model.HookLogQuery
import com.svwh.tools.feature.hookconfig.domain.model.HookLogRecord
import com.svwh.tools.feature.hookconfig.domain.model.HookLogTypeOption

interface FridaLogRepository {
    suspend fun queryLogs(query: HookLogQuery): AppResult<HookLogPageResult>

    suspend fun queryLogDetail(packageName: String, id: Long): AppResult<HookLogRecord?>

    suspend fun deleteAll(packageName: String, selectedLevels: Set<Int>): AppResult<Unit>

    suspend fun deleteByIds(packageName: String, ids: Set<Long>): AppResult<Unit>

    suspend fun queryAvailableLevels(): AppResult<List<HookLogTypeOption>>
}
