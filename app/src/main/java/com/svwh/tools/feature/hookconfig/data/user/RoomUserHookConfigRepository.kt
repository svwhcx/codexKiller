package com.svwh.tools.feature.hookconfig.data.user

import com.svwh.tools.core.common.AppError
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.data.target.TargetConfigDatabase
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigDraft
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigItem
import com.svwh.tools.feature.hookconfig.domain.repository.UserHookConfigRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class RoomUserHookConfigRepository @Inject constructor(
    private val targetConfigDatabase: TargetConfigDatabase,
) : UserHookConfigRepository {

    override suspend fun getConfigs(
        envType: String,
        packageName: String,
    ): AppResult<List<UserHookConfigItem>> = withContext(Dispatchers.IO) {
        runCatching {
            AppResult.Success(
                targetConfigDatabase.getUserConfigs(
                    envType = envType,
                    packageName = packageName,
                    customOnly = true,
                ),
            )
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun getRuntimeConfigs(
        envType: String,
        packageName: String,
    ): AppResult<List<UserHookConfigItem>> = withContext(Dispatchers.IO) {
        runCatching {
            AppResult.Success(
                targetConfigDatabase.getUserConfigs(
                    envType = envType,
                    packageName = packageName,
                    customOnly = false,
                ),
            )
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun getConfigById(
        envType: String,
        packageName: String,
        id: Long,
    ): AppResult<UserHookConfigDraft?> = withContext(Dispatchers.IO) {
        runCatching {
            AppResult.Success(
                targetConfigDatabase.getUserConfigById(
                    envType = envType,
                    packageName = packageName,
                    id = id,
                ),
            )
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun saveConfig(draft: UserHookConfigDraft): AppResult<Long> = withContext(Dispatchers.IO) {
        runCatching {
            validateDraft(draft)
            AppResult.Success(targetConfigDatabase.saveUserConfig(draft))
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun updateEnabled(
        envType: String,
        packageName: String,
        id: Long,
        enabled: Boolean,
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            targetConfigDatabase.updateUserConfigEnabled(
                envType = envType,
                packageName = packageName,
                id = id,
                enabled = enabled,
            )
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun deleteConfigs(
        envType: String,
        packageName: String,
        ids: List<Long>,
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            targetConfigDatabase.deleteUserConfigs(
                envType = envType,
                packageName = packageName,
                ids = ids,
            )
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    private fun validateDraft(draft: UserHookConfigDraft) {
        require(draft.packageName.isNotBlank()) { "packageName cannot be blank" }
        require(draft.envType.isNotBlank()) { "envType cannot be blank" }
        require(draft.configName.isNotBlank()) { "配置名称不能为空" }
        require(draft.className.isNotBlank()) { "类名不能为空" }
        require(draft.methodName.isNotBlank()) { "方法名不能为空" }

        val duplicateNameCount = targetConfigDatabase.countUserConfigName(
            packageName = draft.packageName,
            envType = draft.envType,
            configName = draft.configName.trim(),
            excludeId = draft.id,
        )
        require(duplicateNameCount == 0) { "配置名称已存在" }

        val duplicateSignatureCount = targetConfigDatabase.countUserConfigSignature(
            packageName = draft.packageName,
            envType = draft.envType,
            className = draft.className.trim(),
            methodName = draft.methodName.trim(),
            params = draft.params.trim(),
            excludeId = draft.id,
        )
        require(duplicateSignatureCount == 0) { "相同方法签名的配置已存在" }
    }
}
