package com.svwh.tools.core.hook

import android.os.Environment
import com.svwh.tools.core.database.dao.HookStateDao
import com.svwh.tools.core.database.entity.HookStateEntity
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HookStateRepositoryImpl @Inject constructor(
    private val hookStateDao: HookStateDao,
) : HookStateRepository {
    override suspend fun syncAndGetEnabledPackages(
        environmentType: HookEnvironmentType,
        packageNames: Set<String>,
    ): Set<String> {
        if (packageNames.isEmpty()) return emptySet()

        val now = System.currentTimeMillis()
        val existingStates = hookStateDao.getByPackages(
            envType = environmentType.storageValue,
            packageNames = packageNames,
        ).associateBy { it.packageName }

        val syncedStates = packageNames.map { packageName ->
            val statusFile = getStatusFile(packageName, environmentType)
            val existing = existingStates[packageName]
            val createdAt = existing?.createdAtMillis ?: now
            val enabled = statusFile.exists() || existing?.enabled == true

            HookStateEntity(
                id = existing?.id ?: 0,
                packageName = packageName,
                envType = environmentType.storageValue,
                enabled = enabled,
                statusFilePath = statusFile.absolutePath,
                source = "media_file",
                note = existing?.note,
                createdAtMillis = createdAt,
                updatedAtMillis = now,
                lastSyncedAtMillis = now,
            )
        }

        hookStateDao.upsertSyncedStates(syncedStates)

        return syncedStates
            .asSequence()
            .filter { it.enabled }
            .map { it.packageName }
            .toSet()
    }

    override suspend fun setHookEnabled(
        packageName: String,
        environmentType: HookEnvironmentType,
        enabled: Boolean,
    ): Boolean {
        val statusFile = getStatusFile(packageName, environmentType)
        val fileOperationSucceeded = if (enabled) {
            runCatching {
                statusFile.parentFile?.mkdirs()
                if (!statusFile.exists()) {
                    statusFile.createNewFile()
                }
            }.isSuccess
        } else {
            runCatching {
                if (statusFile.exists()) {
                    statusFile.delete()
                } else {
                    true
                }
            }.getOrDefault(false)
        }

        val actualEnabled = enabled
        val note = if (fileOperationSucceeded) {
            null
        } else {
            "status_file_operation_failed"
        }

        upsertSingleState(
            packageName = packageName,
            environmentType = environmentType,
            enabled = actualEnabled,
            statusFile = statusFile,
            note = note,
        )

        return actualEnabled
    }

    private suspend fun upsertSingleState(
        packageName: String,
        environmentType: HookEnvironmentType,
        enabled: Boolean,
        statusFile: File,
        note: String?,
    ) {
        val now = System.currentTimeMillis()
        val existing = hookStateDao.getByPackage(
            envType = environmentType.storageValue,
            packageName = packageName,
        )

        hookStateDao.insert(
            HookStateEntity(
                id = existing?.id ?: 0,
                packageName = packageName,
                envType = environmentType.storageValue,
                enabled = enabled,
                statusFilePath = statusFile.absolutePath,
                source = "media_file",
                note = note,
                createdAtMillis = existing?.createdAtMillis ?: now,
                updatedAtMillis = now,
                lastSyncedAtMillis = now,
            ),
        )
    }

    private fun getStatusFile(
        packageName: String,
        environmentType: HookEnvironmentType,
    ): File {
        val root = Environment.getExternalStorageDirectory()
        return File(
            root,
            "Android/media/$packageName/stool/${environmentType.enableFileName}",
        )
    }
}
