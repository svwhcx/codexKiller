package com.svwh.tools.feature.hookconfig.data.frida

import com.svwh.tools.core.common.AppError
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.data.target.TargetConfigDatabase
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptDraft
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem
import com.svwh.tools.feature.hookconfig.domain.repository.FridaScriptRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class RoomFridaScriptRepository @Inject constructor(
    private val targetConfigDatabase: TargetConfigDatabase,
    private val noEnvFridaScriptExporter: NoEnvFridaScriptExporter,
) : FridaScriptRepository {

    override suspend fun getScripts(
        envType: String,
        packageName: String,
    ): AppResult<List<FridaScriptItem>> = withContext(Dispatchers.IO) {
        runCatching {
            AppResult.Success(
                targetConfigDatabase.getFridaScripts(
                    envType = envType,
                    packageName = packageName,
                ),
            )
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun getScriptById(
        envType: String,
        packageName: String,
        id: Long,
    ): AppResult<FridaScriptDraft?> = withContext(Dispatchers.IO) {
        runCatching {
            AppResult.Success(
                targetConfigDatabase.getFridaScriptById(
                    envType = envType,
                    packageName = packageName,
                    id = id,
                ),
            )
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun saveScript(draft: FridaScriptDraft): AppResult<Long> = withContext(Dispatchers.IO) {
        runCatching {
            validateDraft(draft)
            val savedId = targetConfigDatabase.saveFridaScript(draft)
            syncRuntimeScripts(draft.envType, draft.packageName)
            AppResult.Success(savedId)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun updateEnabled(
        envType: String,
        packageName: String,
        id: Long,
        enabled: Boolean,
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            targetConfigDatabase.updateFridaScriptEnabled(
                envType = envType,
                packageName = packageName,
                id = id,
                enabled = enabled,
            )
            syncRuntimeScripts(envType, packageName)
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun deleteScripts(
        envType: String,
        packageName: String,
        ids: List<Long>,
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            targetConfigDatabase.deleteFridaScripts(
                envType = envType,
                packageName = packageName,
                ids = ids,
            )
            syncRuntimeScripts(envType, packageName)
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    private fun validateDraft(draft: FridaScriptDraft) {
        require(draft.packageName.isNotBlank()) { "packageName cannot be blank" }
        require(draft.envType.isNotBlank()) { "envType cannot be blank" }
        require(draft.name.isNotBlank()) { "脚本名称不能为空" }

        val duplicateNameCount = targetConfigDatabase.countFridaScriptName(
            packageName = draft.packageName,
            envType = draft.envType,
            name = draft.name.trim(),
            excludeId = draft.id,
        )
        require(duplicateNameCount == 0) { "脚本名称已存在" }
    }

    private fun syncRuntimeScripts(envType: String, packageName: String) {
        noEnvFridaScriptExporter.syncPackage(
            envType = envType,
            packageName = packageName,
            scripts = targetConfigDatabase.getFridaScripts(
                envType = envType,
                packageName = packageName,
            ),
        )
    }
}
