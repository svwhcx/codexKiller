package com.svwh.tools.feature.hookconfig.domain.repository

import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigDraft
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigItem

interface UserHookConfigRepository {
    suspend fun getConfigs(
        envType: String,
        packageName: String,
    ): AppResult<List<UserHookConfigItem>>

    suspend fun getConfigById(id: Long): AppResult<UserHookConfigDraft?>

    suspend fun saveConfig(draft: UserHookConfigDraft): AppResult<Long>

    suspend fun updateEnabled(
        id: Long,
        enabled: Boolean,
    ): AppResult<Unit>

    suspend fun deleteConfigs(ids: List<Long>): AppResult<Unit>
}
