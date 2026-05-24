package com.svwh.tools.feature.hookconfig.data.frida

import com.svwh.tools.core.common.AppError
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.core.database.dao.FridaScriptDao
import com.svwh.tools.core.database.entity.FridaScriptEntity
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptDraft
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem
import com.svwh.tools.feature.hookconfig.domain.repository.FridaScriptRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class RoomFridaScriptRepository @Inject constructor(
    private val fridaScriptDao: FridaScriptDao,
) : FridaScriptRepository {

    override suspend fun getScripts(
        envType: String,
        packageName: String,
    ): AppResult<List<FridaScriptItem>> = withContext(Dispatchers.IO) {
        runCatching {
            AppResult.Success(
                fridaScriptDao.getScripts(
                    packageName = packageName,
                    envType = envType,
                ).map { entity -> entity.toItem() },
            )
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun getScriptById(id: Long): AppResult<FridaScriptDraft?> = withContext(Dispatchers.IO) {
        runCatching {
            AppResult.Success(fridaScriptDao.getScriptById(id)?.toDraft())
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun saveScript(draft: FridaScriptDraft): AppResult<Long> = withContext(Dispatchers.IO) {
        runCatching {
            validateDraft(draft)
            val now = System.currentTimeMillis()
            val existing = if (draft.id > 0) fridaScriptDao.getScriptById(draft.id) else null
            val entity = FridaScriptEntity(
                id = draft.id,
                packageName = draft.packageName,
                envType = draft.envType,
                name = draft.name.trim(),
                scriptContent = draft.scriptContent,
                enabled = draft.enabled,
                createdAtMillis = existing?.createdAtMillis ?: now,
                updatedAtMillis = now,
            )
            val savedId = if (draft.id > 0) {
                fridaScriptDao.updateScript(entity)
                draft.id
            } else {
                fridaScriptDao.insertScript(entity)
            }
            AppResult.Success(savedId)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun updateEnabled(
        id: Long,
        enabled: Boolean,
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            fridaScriptDao.updateEnabled(
                id = id,
                enabled = enabled,
                updatedAtMillis = System.currentTimeMillis(),
            )
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun deleteScripts(ids: List<Long>): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (ids.isNotEmpty()) {
                fridaScriptDao.deleteScriptsByIds(ids)
            }
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    private suspend fun validateDraft(draft: FridaScriptDraft) {
        require(draft.packageName.isNotBlank()) { "packageName cannot be blank" }
        require(draft.envType.isNotBlank()) { "envType cannot be blank" }
        require(draft.name.isNotBlank()) { "\u811a\u672c\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a" }

        val duplicateNameCount = fridaScriptDao.countScriptName(
            packageName = draft.packageName,
            envType = draft.envType,
            name = draft.name.trim(),
            excludeId = draft.id,
        )
        require(duplicateNameCount == 0) { "\u811a\u672c\u540d\u79f0\u5df2\u5b58\u5728" }
    }

    private fun FridaScriptEntity.toItem(): FridaScriptItem {
        return FridaScriptItem(
            id = id,
            packageName = packageName,
            envType = envType,
            name = name,
            scriptContent = scriptContent,
            enabled = enabled,
        )
    }

    private fun FridaScriptEntity.toDraft(): FridaScriptDraft {
        return FridaScriptDraft(
            id = id,
            packageName = packageName,
            envType = envType,
            name = name,
            scriptContent = scriptContent,
            enabled = enabled,
        )
    }
}
