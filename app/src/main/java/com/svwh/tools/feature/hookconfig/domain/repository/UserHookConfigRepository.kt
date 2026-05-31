package com.svwh.tools.feature.hookconfig.domain.repository

import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigDraft
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigItem

interface UserHookConfigRepository {
    suspend fun getConfigs(
        envType: String,
        packageName: String,
    ): AppResult<List<UserHookConfigItem>>

    suspend fun getRuntimeConfigs(
        envType: String,
        packageName: String,
    ): AppResult<List<UserHookConfigItem>>

    suspend fun getConfigById(
        envType: String,
        packageName: String,
        id: Long,
    ): AppResult<UserHookConfigDraft?>

    suspend fun saveConfig(draft: UserHookConfigDraft): AppResult<Long>

    suspend fun updateEnabled(
        envType: String,
        packageName: String,
        id: Long,
        enabled: Boolean,
    ): AppResult<Unit>

    suspend fun deleteConfigs(
        envType: String,
        packageName: String,
        ids: List<Long>,
    ): AppResult<Unit>
}
