package com.svwh.tools.feature.hookconfig.domain.repository

import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptDraft
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem

interface FridaScriptRepository {
    suspend fun getScripts(
        envType: String,
        packageName: String,
    ): AppResult<List<FridaScriptItem>>

    suspend fun getScriptById(
        envType: String,
        packageName: String,
        id: Long,
    ): AppResult<FridaScriptDraft?>

    suspend fun saveScript(draft: FridaScriptDraft): AppResult<Long>

    suspend fun updateEnabled(
        envType: String,
        packageName: String,
        id: Long,
        enabled: Boolean,
    ): AppResult<Unit>

    suspend fun deleteScripts(
        envType: String,
        packageName: String,
        ids: List<Long>,
    ): AppResult<Unit>
}
