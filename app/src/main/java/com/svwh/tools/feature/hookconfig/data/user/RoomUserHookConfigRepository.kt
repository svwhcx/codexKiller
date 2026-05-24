package com.svwh.tools.feature.hookconfig.data.user

import com.svwh.tools.core.common.AppError
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.core.database.dao.UserHookConfigDao
import com.svwh.tools.core.database.dao.UserHookConfigWithRules
import com.svwh.tools.core.database.entity.ChangeValueRuleEntity
import com.svwh.tools.core.database.entity.UserHookConfigEntity
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigDraft
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigItem
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigRule
import com.svwh.tools.feature.hookconfig.domain.repository.UserHookConfigRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class RoomUserHookConfigRepository @Inject constructor(
    private val userHookConfigDao: UserHookConfigDao,
) : UserHookConfigRepository {

    override suspend fun getConfigs(
        envType: String,
        packageName: String,
    ): AppResult<List<UserHookConfigItem>> = withContext(Dispatchers.IO) {
        runCatching {
            AppResult.Success(
                userHookConfigDao.getConfigsWithRules(
                    packageName = packageName,
                    envType = envType,
                ).map { relation ->
                    UserHookConfigItem(
                        id = relation.config.id,
                        packageName = relation.config.packageName,
                        envType = relation.config.envType,
                        configName = relation.config.configName,
                        className = relation.config.className,
                        methodName = relation.config.methodName,
                        params = relation.config.params,
                        enabled = relation.config.enabled,
                        isLog = relation.config.isLog,
                        isInterrupted = relation.config.isInterrupted,
                        ruleCount = relation.rules.size,
                    )
                },
            )
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun getConfigById(id: Long): AppResult<UserHookConfigDraft?> = withContext(Dispatchers.IO) {
        runCatching {
            AppResult.Success(userHookConfigDao.getConfigWithRulesById(id)?.toDraft())
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun saveConfig(draft: UserHookConfigDraft): AppResult<Long> = withContext(Dispatchers.IO) {
        runCatching {
            validateDraft(draft)
            val now = System.currentTimeMillis()
            val existing = if (draft.id > 0) userHookConfigDao.getConfigWithRulesById(draft.id) else null
            val entity = UserHookConfigEntity(
                id = draft.id,
                packageName = draft.packageName,
                envType = draft.envType,
                configName = draft.configName.trim(),
                className = draft.className.trim(),
                methodName = draft.methodName.trim(),
                params = draft.params.trim(),
                methodSignature = draft.methodSignature,
                invokeClass = draft.invokeClass,
                hookStatus = draft.hookStatus,
                isLog = draft.isLog,
                isInterrupted = draft.isInterrupted,
                enabled = draft.enabled,
                exp = draft.exp,
                type = draft.type,
                createdAtMillis = existing?.config?.createdAtMillis ?: now,
                updatedAtMillis = now,
            )
            val savedId = if (draft.id > 0) {
                userHookConfigDao.updateConfig(entity)
                draft.id
            } else {
                userHookConfigDao.insertConfig(entity)
            }
            userHookConfigDao.deleteRulesByHookConfigId(savedId)
            val rules = draft.rules.map { rule ->
                ChangeValueRuleEntity(
                    hookConfigId = savedId,
                    rule = rule.rule,
                    paramNumber = rule.paramNumber,
                    matchValue = rule.matchValue,
                    replaceValue = rule.replaceValue,
                )
            }
            if (rules.isNotEmpty()) {
                userHookConfigDao.insertRules(rules)
            }
            AppResult.Success(savedId)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun updateEnabled(
        id: Long,
        enabled: Boolean,
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            userHookConfigDao.updateEnabled(
                id = id,
                enabled = enabled,
                updatedAtMillis = System.currentTimeMillis(),
            )
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun deleteConfigs(ids: List<Long>): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (ids.isNotEmpty()) {
                userHookConfigDao.deleteRulesByHookConfigIds(ids)
                userHookConfigDao.deleteConfigsByIds(ids)
            }
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    private suspend fun validateDraft(draft: UserHookConfigDraft) {
        require(draft.packageName.isNotBlank()) { "packageName cannot be blank" }
        require(draft.envType.isNotBlank()) { "envType cannot be blank" }
        require(draft.configName.isNotBlank()) { "配置名称不能为空" }
        require(draft.className.isNotBlank()) { "类名不能为空" }
        require(draft.methodName.isNotBlank()) { "方法名不能为空" }

        val duplicateNameCount = userHookConfigDao.countConfigName(
            packageName = draft.packageName,
            envType = draft.envType,
            configName = draft.configName.trim(),
            excludeId = draft.id,
        )
        require(duplicateNameCount == 0) { "配置名称已存在" }

        val duplicateSignatureCount = userHookConfigDao.countSignature(
            packageName = draft.packageName,
            envType = draft.envType,
            className = draft.className.trim(),
            methodName = draft.methodName.trim(),
            params = draft.params.trim(),
            excludeId = draft.id,
        )
        require(duplicateSignatureCount == 0) { "相同方法签名的配置已存在" }
    }

    private fun UserHookConfigWithRules.toDraft(): UserHookConfigDraft {
        return UserHookConfigDraft(
            id = config.id,
            packageName = config.packageName,
            envType = config.envType,
            configName = config.configName,
            className = config.className,
            methodName = config.methodName,
            params = config.params,
            methodSignature = config.methodSignature,
            invokeClass = config.invokeClass,
            hookStatus = config.hookStatus,
            isLog = config.isLog,
            isInterrupted = config.isInterrupted,
            enabled = config.enabled,
            exp = config.exp,
            type = config.type,
            rules = rules.map { rule ->
                UserHookConfigRule(
                    id = rule.id,
                    hookConfigId = rule.hookConfigId,
                    rule = rule.rule,
                    paramNumber = rule.paramNumber,
                    matchValue = rule.matchValue,
                    replaceValue = rule.replaceValue,
                )
            },
        )
    }
}
