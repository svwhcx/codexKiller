package com.svwh.tools.feature.hookconfig.data.user

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import com.svwh.tools.core.database.dao.UserHookConfigDao
import com.svwh.tools.core.database.dao.UserHookConfigWithRules
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class NoEnvHookConfigExporter @Inject constructor(
    private val userHookConfigDao: UserHookConfigDao,
) {
    suspend fun syncPackage(
        envType: String,
        packageName: String,
    ) = withContext(Dispatchers.IO) {
        if (packageName.isBlank() || !envType.isRuntimeSupported()) return@withContext
        val configs = userHookConfigDao.getConfigsWithRules(
            packageName = packageName,
            envType = envType,
        )
        writeRuntimeDatabase(
            packageName = packageName,
            envType = envType,
            configs = configs,
        )
    }

    private fun writeRuntimeDatabase(
        packageName: String,
        envType: String,
        configs: List<UserHookConfigWithRules>,
    ) {
        val dbFile = runtimeDatabaseFile(packageName)
        ensureRuntimeDatabaseFile(dbFile)
        val runtimeEnvType = envType.toRuntimeEnvType()

        SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY,
        ).use { db ->
            db.beginTransaction()
            try {
                ensureSchema(db)
                val existingIds = queryRuntimeConfigIds(db, packageName, runtimeEnvType)
                if (existingIds.isNotEmpty()) {
                    db.delete(
                        CHANGE_VALUE_TABLE,
                        "hookConfigId IN (${existingIds.joinToString(",") { "?" }})",
                        existingIds.map { it.toString() }.toTypedArray(),
                    )
                }
                db.delete(
                    APP_HOOK_TABLE,
                    "envType = ? AND packageName = ?",
                    arrayOf(runtimeEnvType.toString(), packageName),
                )

                configs
                    .filter { relation -> relation.config.enabled }
                    .forEach { relation ->
                        db.insert(APP_HOOK_TABLE, null, relation.toRuntimeConfigValues(runtimeEnvType))
                        relation.rules.forEach { rule ->
                            db.insert(
                                CHANGE_VALUE_TABLE,
                                null,
                                ContentValues().apply {
                                    put("id", rule.id)
                                    put("hookConfigId", relation.config.id)
                                    put("rule", rule.rule)
                                    put("paramNumber", rule.paramNumber)
                                    put("replaceValue", rule.replaceValue)
                                    put("matchValue", rule.matchValue)
                                },
                            )
                        }
                    }

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun ensureRuntimeDatabaseFile(dbFile: File) {
        val parent = dbFile.parentFile
            ?: throw IOException("Runtime database parent directory is missing: ${dbFile.absolutePath}")
        if (!parent.exists() && !parent.mkdirs()) {
            throw IOException("Cannot create runtime database directory: ${parent.absolutePath}")
        }
        if (!parent.isDirectory) {
            throw IOException("Runtime database parent is not a directory: ${parent.absolutePath}")
        }
        if (!dbFile.exists() && !dbFile.createNewFile()) {
            throw IOException("Cannot create runtime database file: ${dbFile.absolutePath}")
        }
    }

    private fun ensureSchema(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS app_hook_config (
                id INTEGER PRIMARY KEY,
                configName TEXT NOT NULL,
                methodSignature TEXT NOT NULL DEFAULT '',
                packageName TEXT NOT NULL,
                className TEXT NOT NULL,
                methodName TEXT NOT NULL,
                params TEXT NOT NULL,
                invokeClass BLOB NOT NULL DEFAULT X'',
                envType INTEGER NOT NULL,
                isLog INTEGER NOT NULL,
                hookStatus INTEGER NOT NULL,
                isInterrupted INTEGER NOT NULL,
                enable INTEGER NOT NULL,
                exp TEXT NOT NULL DEFAULT '',
                type INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS change_value_config (
                id INTEGER PRIMARY KEY,
                hookConfigId INTEGER NOT NULL,
                rule TEXT NOT NULL,
                paramNumber INTEGER NOT NULL,
                replaceValue TEXT NOT NULL,
                matchValue TEXT NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_app_hook_config_package_env
            ON app_hook_config(packageName, envType)
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_change_value_config_hookConfigId
            ON change_value_config(hookConfigId)
            """.trimIndent(),
        )
    }

    private fun queryRuntimeConfigIds(
        db: SQLiteDatabase,
        packageName: String,
        runtimeEnvType: Int,
    ): List<Long> {
        val ids = mutableListOf<Long>()
        db.rawQuery(
            "SELECT id FROM app_hook_config WHERE envType = ? AND packageName = ?",
            arrayOf(runtimeEnvType.toString(), packageName),
        ).use { cursor ->
            while (cursor.moveToNext()) {
                ids.add(cursor.getLong(0))
            }
        }
        return ids
    }

    private fun UserHookConfigWithRules.toRuntimeConfigValues(runtimeEnvType: Int): ContentValues {
        val entity = config
        return ContentValues().apply {
            put("id", entity.id)
            put("configName", entity.configName)
            put("methodSignature", entity.methodSignature)
            put("packageName", entity.packageName)
            put("className", entity.className)
            put("methodName", entity.methodName)
            put("params", entity.params)
            put("invokeClass", entity.invokeClass)
            put("envType", runtimeEnvType)
            put("isLog", entity.isLog.toInt())
            put("hookStatus", entity.hookStatus.toInt())
            put("isInterrupted", entity.isInterrupted.toInt())
            put("enable", entity.enabled.toInt())
            put("exp", entity.exp)
            put("type", entity.type.toRuntimeType())
        }
    }

    private fun runtimeDatabaseFile(packageName: String): File {
        return File(
            Environment.getExternalStorageDirectory(),
            "Android/media/$packageName/stool/$RUNTIME_DATABASE_NAME",
        )
    }

    private fun String.toRuntimeEnvType(): Int {
        return if (this == NO_ENV_STORAGE_VALUE) 0 else 1
    }

    private fun String.isRuntimeSupported(): Boolean {
        return this == NO_ENV_STORAGE_VALUE || this == WITH_ENV_STORAGE_VALUE
    }

    private fun String.toRuntimeType(): Int {
        return trim().toIntOrNull() ?: 0
    }

    private fun Boolean.toInt(): Int = if (this) 1 else 0

    private companion object {
        const val NO_ENV_STORAGE_VALUE = "no_env"
        const val WITH_ENV_STORAGE_VALUE = "with_env"
        const val RUNTIME_DATABASE_NAME = "killer_hook.db"
        const val APP_HOOK_TABLE = "app_hook_config"
        const val CHANGE_VALUE_TABLE = "change_value_config"
    }
}
