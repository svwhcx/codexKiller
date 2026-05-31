package com.svwh.tools.feature.hookconfig.data.target

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Environment
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptDraft
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem
import com.svwh.tools.feature.hookconfig.domain.model.GlobalFridaScriptScope
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigDraft
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigItem
import com.svwh.tools.feature.hookconfig.domain.model.UserHookConfigRule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TargetConfigDatabase @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun getUserConfigs(
        envType: String,
        packageName: String,
        customOnly: Boolean,
    ): List<UserHookConfigItem> = openDatabase(packageName).use { db ->
        val runtimeEnvType = envType.toRuntimeEnvType()
        val selection = if (customOnly) {
            "envType = ? AND packageName = ? AND (type = ? OR type IS NULL)"
        } else {
            "envType = ? AND packageName = ?"
        }
        val selectionArgs = if (customOnly) {
            arrayOf(runtimeEnvType.toString(), packageName, CUSTOM_HOOK_TYPE)
        } else {
            arrayOf(runtimeEnvType.toString(), packageName)
        }
        db.query(
            APP_HOOK_TABLE,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "updatedAtMillis DESC, id DESC",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toUserHookConfigItem(db))
                }
            }
        }
    }

    fun getUserConfigById(
        envType: String,
        packageName: String,
        id: Long,
    ): UserHookConfigDraft? = openDatabase(packageName).use { db ->
        db.query(
            APP_HOOK_TABLE,
            null,
            "id = ? AND envType = ? AND packageName = ?",
            arrayOf(id.toString(), envType.toRuntimeEnvType().toString(), packageName),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toUserHookConfigDraft(db) else null
        }
    }

    fun saveUserConfig(draft: UserHookConfigDraft): Long = openDatabase(draft.packageName).use { db ->
        val now = System.currentTimeMillis()
        db.beginTransaction()
        try {
            val existing = if (draft.id > 0) {
                queryUserConfigDraft(db, draft.packageName, draft.envType, draft.id)
            } else {
                null
            }
            val values = ContentValues().apply {
                if (draft.id > 0) put("id", draft.id)
                put("configName", draft.configName.trim())
                put("methodSignature", draft.methodSignature)
                put("packageName", draft.packageName)
                put("className", draft.className.trim())
                put("methodName", draft.methodName.trim())
                put("params", draft.params.trim())
                put("invokeClass", draft.invokeClass)
                put("envType", draft.envType.toRuntimeEnvType())
                put("isLog", draft.isLog.toInt())
                put("hookStatus", draft.hookStatus.toInt())
                put("isInterrupted", draft.isInterrupted.toInt())
                put("enable", draft.enabled.toInt())
                put("exp", draft.exp)
                put("type", draft.type.toRuntimeType())
                put("createdAtMillis", existing?.createdAtMillis ?: now)
                put("updatedAtMillis", now)
            }
            val savedId = if (draft.id > 0) {
                db.update(APP_HOOK_TABLE, values, "id = ?", arrayOf(draft.id.toString()))
                draft.id
            } else {
                db.insertOrThrow(APP_HOOK_TABLE, null, values)
            }
            db.delete(CHANGE_VALUE_TABLE, "hookConfigId = ?", arrayOf(savedId.toString()))
            draft.rules.forEach { rule ->
                db.insertOrThrow(
                    CHANGE_VALUE_TABLE,
                    null,
                    ContentValues().apply {
                        put("hookConfigId", savedId)
                        put("rule", rule.rule)
                        put("paramNumber", rule.paramNumber)
                        put("replaceValue", rule.replaceValue)
                        put("matchValue", rule.matchValue)
                    },
                )
            }
            db.setTransactionSuccessful()
            savedId
        } finally {
            db.endTransaction()
        }
    }

    fun updateUserConfigEnabled(
        envType: String,
        packageName: String,
        id: Long,
        enabled: Boolean,
    ) = openDatabase(packageName).use { db ->
        db.update(
            APP_HOOK_TABLE,
            ContentValues().apply {
                put("enable", enabled.toInt())
                put("updatedAtMillis", System.currentTimeMillis())
            },
            "id = ? AND envType = ? AND packageName = ?",
            arrayOf(id.toString(), envType.toRuntimeEnvType().toString(), packageName),
        )
    }

    fun deleteUserConfigs(
        envType: String,
        packageName: String,
        ids: List<Long>,
    ) = openDatabase(packageName).use { db ->
        if (ids.isEmpty()) return@use
        val placeholders = ids.joinToString(",") { "?" }
        val idArgs = ids.map { it.toString() }.toTypedArray()
        db.beginTransaction()
        try {
            db.delete(CHANGE_VALUE_TABLE, "hookConfigId IN ($placeholders)", idArgs)
            db.delete(
                APP_HOOK_TABLE,
                "id IN ($placeholders) AND envType = ? AND packageName = ?",
                idArgs + arrayOf(envType.toRuntimeEnvType().toString(), packageName),
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun countUserConfigName(
        envType: String,
        packageName: String,
        configName: String,
        excludeId: Long,
    ): Int = openDatabase(packageName).use { db ->
        db.count(
            APP_HOOK_TABLE,
            """
            envType = ? AND packageName = ? AND configName = ?
            AND (? <= 0 OR id != ?)
            """.trimIndent(),
            arrayOf(
                envType.toRuntimeEnvType().toString(),
                packageName,
                configName,
                excludeId.toString(),
                excludeId.toString(),
            ),
        )
    }

    fun countUserConfigSignature(
        envType: String,
        packageName: String,
        className: String,
        methodName: String,
        params: String,
        excludeId: Long,
    ): Int = openDatabase(packageName).use { db ->
        db.count(
            APP_HOOK_TABLE,
            """
            envType = ? AND packageName = ? AND className = ? AND methodName = ? AND params = ?
            AND (? <= 0 OR id != ?)
            """.trimIndent(),
            arrayOf(
                envType.toRuntimeEnvType().toString(),
                packageName,
                className,
                methodName,
                params,
                excludeId.toString(),
                excludeId.toString(),
            ),
        )
    }

    fun getFridaScripts(
        envType: String,
        packageName: String,
    ): List<FridaScriptItem> = openDatabase(packageName).use { db ->
        db.query(
            FRIDA_SCRIPTS_TABLE,
            null,
            "packageName = ? AND envType = ?",
            arrayOf(packageName, envType),
            null,
            null,
            "updatedAtMillis DESC, id DESC",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.toFridaScriptItem())
                }
            }
        }
    }

    fun getFridaScriptById(
        envType: String,
        packageName: String,
        id: Long,
    ): FridaScriptDraft? = openDatabase(packageName).use { db ->
        db.query(
            FRIDA_SCRIPTS_TABLE,
            null,
            "id = ? AND packageName = ? AND envType = ?",
            arrayOf(id.toString(), packageName, envType),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.toFridaScriptDraft() else null
        }
    }

    fun saveFridaScript(draft: FridaScriptDraft): Long = openDatabase(draft.packageName).use { db ->
        val now = System.currentTimeMillis()
        val existingCreatedAt = if (draft.id > 0) {
            queryFridaCreatedAtMillis(db, draft.envType, draft.packageName, draft.id)
        } else {
            null
        }
        val values = ContentValues().apply {
            if (draft.id > 0) put("id", draft.id)
            put("packageName", draft.packageName)
            put("envType", draft.envType)
            put("name", draft.name.trim())
            put("scriptContent", draft.scriptContent)
            put("enabled", draft.enabled.toInt())
            put("createdAtMillis", existingCreatedAt ?: now)
            put("updatedAtMillis", now)
        }
        if (draft.id > 0) {
            db.update(FRIDA_SCRIPTS_TABLE, values, "id = ?", arrayOf(draft.id.toString()))
            draft.id
        } else {
            db.insertOrThrow(FRIDA_SCRIPTS_TABLE, null, values)
        }
    }

    fun updateFridaScriptEnabled(
        envType: String,
        packageName: String,
        id: Long,
        enabled: Boolean,
    ) = openDatabase(packageName).use { db ->
        db.update(
            FRIDA_SCRIPTS_TABLE,
            ContentValues().apply {
                put("enabled", enabled.toInt())
                put("updatedAtMillis", System.currentTimeMillis())
            },
            "id = ? AND packageName = ? AND envType = ?",
            arrayOf(id.toString(), packageName, envType),
        )
    }

    fun deleteFridaScripts(
        envType: String,
        packageName: String,
        ids: List<Long>,
    ) = openDatabase(packageName).use { db ->
        if (ids.isEmpty()) return@use
        val placeholders = ids.joinToString(",") { "?" }
        val idArgs = ids.map { it.toString() }.toTypedArray()
        db.delete(
            FRIDA_SCRIPTS_TABLE,
            "id IN ($placeholders) AND packageName = ? AND envType = ?",
            idArgs + arrayOf(packageName, envType),
        )
    }

    fun countFridaScriptName(
        envType: String,
        packageName: String,
        name: String,
        excludeId: Long,
    ): Int = openDatabase(packageName).use { db ->
        db.count(
            FRIDA_SCRIPTS_TABLE,
            """
            packageName = ? AND envType = ? AND name = ?
            AND (? <= 0 OR id != ?)
            """.trimIndent(),
            arrayOf(packageName, envType, name, excludeId.toString(), excludeId.toString()),
        )
    }

    private fun openDatabase(packageName: String): SQLiteDatabase {
        val dbFile = databaseFile(packageName)
        ensureParentDirectory(dbFile)
        return SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY,
        ).also { db -> ensureSchema(db) }
    }

    private fun databaseFile(packageName: String): File {
        return File(configDirectory(packageName), RUNTIME_DATABASE_NAME)
    }

    private fun configDirectory(packageName: String): File {
        return File(
            Environment.getExternalStorageDirectory(),
            "Android/media/${packageName.toStoragePackageName()}/stool",
        )
    }

    private fun String.toStoragePackageName(): String {
        return if (this == GlobalFridaScriptScope.PACKAGE_NAME) {
            context.packageName
        } else {
            this
        }
    }

    private fun ensureParentDirectory(file: File) {
        val parent = file.parentFile
            ?: throw IOException("Config database parent directory is missing: ${file.absolutePath}")
        if (!parent.exists() && !parent.mkdirs()) {
            throw IOException("Cannot create config database directory: ${parent.absolutePath}")
        }
        if (!parent.isDirectory) {
            throw IOException("Config database parent is not a directory: ${parent.absolutePath}")
        }
    }

    private fun ensureSchema(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $APP_HOOK_TABLE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
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
                type INTEGER NOT NULL DEFAULT 0,
                createdAtMillis INTEGER NOT NULL DEFAULT 0,
                updatedAtMillis INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQLIgnoringDuplicateColumn(
            "ALTER TABLE $APP_HOOK_TABLE ADD COLUMN createdAtMillis INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQLIgnoringDuplicateColumn(
            "ALTER TABLE $APP_HOOK_TABLE ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0",
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $CHANGE_VALUE_TABLE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
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
            CREATE TABLE IF NOT EXISTS $FRIDA_SCRIPTS_TABLE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                packageName TEXT NOT NULL,
                envType TEXT NOT NULL,
                name TEXT NOT NULL,
                scriptContent TEXT NOT NULL,
                enabled INTEGER NOT NULL,
                createdAtMillis INTEGER NOT NULL,
                updatedAtMillis INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_app_hook_config_package_env ON $APP_HOOK_TABLE(packageName, envType)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_change_value_config_hookConfigId ON $CHANGE_VALUE_TABLE(hookConfigId)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_frida_scripts_package_env ON $FRIDA_SCRIPTS_TABLE(packageName, envType)",
        )
    }

    private fun SQLiteDatabase.execSQLIgnoringDuplicateColumn(sql: String) {
        runCatching { execSQL(sql) }
    }

    private fun queryUserConfigDraft(
        db: SQLiteDatabase,
        packageName: String,
        envType: String,
        id: Long,
    ): StoredUserConfigDraft? {
        return db.query(
            APP_HOOK_TABLE,
            null,
            "id = ? AND envType = ? AND packageName = ?",
            arrayOf(id.toString(), envType.toRuntimeEnvType().toString(), packageName),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                StoredUserConfigDraft(
                    draft = cursor.toUserHookConfigDraft(db),
                    createdAtMillis = cursor.getLongOrDefault("createdAtMillis"),
                )
            } else {
                null
            }
        }
    }

    private fun Cursor.toUserHookConfigItem(db: SQLiteDatabase): UserHookConfigItem {
        val id = getLongOrThrow("id")
        return UserHookConfigItem(
            id = id,
            packageName = getStringOrEmpty("packageName"),
            envType = getIntOrDefault("envType").toStorageEnvType(),
            configName = getStringOrEmpty("configName"),
            className = getStringOrEmpty("className"),
            methodName = getStringOrEmpty("methodName"),
            params = getStringOrEmpty("params"),
            type = getIntOrDefault("type").toString(),
            enabled = getIntOrDefault("enable") == 1,
            isLog = getIntOrDefault("isLog") == 1,
            isInterrupted = getIntOrDefault("isInterrupted") == 1,
            ruleCount = db.count(CHANGE_VALUE_TABLE, "hookConfigId = ?", arrayOf(id.toString())),
        )
    }

    private fun Cursor.toUserHookConfigDraft(db: SQLiteDatabase): UserHookConfigDraft {
        val id = getLongOrThrow("id")
        return UserHookConfigDraft(
            id = id,
            packageName = getStringOrEmpty("packageName"),
            envType = getIntOrDefault("envType").toStorageEnvType(),
            configName = getStringOrEmpty("configName"),
            className = getStringOrEmpty("className"),
            methodName = getStringOrEmpty("methodName"),
            params = getStringOrEmpty("params"),
            methodSignature = getStringOrEmpty("methodSignature"),
            invokeClass = getBlobOrEmpty("invokeClass"),
            hookStatus = getIntOrDefault("hookStatus") == 1,
            isLog = getIntOrDefault("isLog") == 1,
            isInterrupted = getIntOrDefault("isInterrupted") == 1,
            enabled = getIntOrDefault("enable") == 1,
            exp = getStringOrEmpty("exp"),
            type = getIntOrDefault("type").toString(),
            rules = queryRules(db, id),
        )
    }

    private fun queryRules(db: SQLiteDatabase, hookConfigId: Long): List<UserHookConfigRule> {
        return db.query(
            CHANGE_VALUE_TABLE,
            null,
            "hookConfigId = ?",
            arrayOf(hookConfigId.toString()),
            null,
            null,
            "id ASC",
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        UserHookConfigRule(
                            id = cursor.getLongOrThrow("id"),
                            hookConfigId = cursor.getLongOrThrow("hookConfigId"),
                            rule = cursor.getStringOrEmpty("rule"),
                            paramNumber = cursor.getIntOrDefault("paramNumber"),
                            matchValue = cursor.getStringOrEmpty("matchValue"),
                            replaceValue = cursor.getStringOrEmpty("replaceValue"),
                        ),
                    )
                }
            }
        }
    }

    private fun queryFridaCreatedAtMillis(
        db: SQLiteDatabase,
        envType: String,
        packageName: String,
        id: Long,
    ): Long? {
        return db.query(
            FRIDA_SCRIPTS_TABLE,
            arrayOf("createdAtMillis"),
            "id = ? AND packageName = ? AND envType = ?",
            arrayOf(id.toString(), packageName, envType),
            null,
            null,
            null,
            "1",
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getLongOrDefault("createdAtMillis") else null
        }
    }

    private fun Cursor.toFridaScriptItem(): FridaScriptItem {
        return FridaScriptItem(
            id = getLongOrThrow("id"),
            packageName = getStringOrEmpty("packageName"),
            envType = getStringOrEmpty("envType"),
            name = getStringOrEmpty("name"),
            scriptContent = getStringOrEmpty("scriptContent"),
            enabled = getIntOrDefault("enabled") == 1,
        )
    }

    private fun Cursor.toFridaScriptDraft(): FridaScriptDraft {
        return FridaScriptDraft(
            id = getLongOrThrow("id"),
            packageName = getStringOrEmpty("packageName"),
            envType = getStringOrEmpty("envType"),
            name = getStringOrEmpty("name"),
            scriptContent = getStringOrEmpty("scriptContent"),
            enabled = getIntOrDefault("enabled") == 1,
        )
    }

    private fun SQLiteDatabase.count(
        table: String,
        selection: String,
        selectionArgs: Array<String>,
    ): Int {
        rawQuery("SELECT COUNT(1) FROM $table WHERE $selection", selectionArgs).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
    }

    private fun Cursor.getLongOrThrow(columnName: String): Long {
        return getLong(getColumnIndexOrThrow(columnName))
    }

    private fun Cursor.getStringOrEmpty(columnName: String): String {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getString(index).orEmpty() else ""
    }

    private fun Cursor.getIntOrDefault(columnName: String, defaultValue: Int = 0): Int {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getInt(index) else defaultValue
    }

    private fun Cursor.getLongOrDefault(columnName: String, defaultValue: Long = 0L): Long {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getLong(index) else defaultValue
    }

    private fun Cursor.getBlobOrEmpty(columnName: String): ByteArray {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getBlob(index) else ByteArray(0)
    }

    private fun String.toRuntimeEnvType(): Int {
        return if (this == NO_ENV_STORAGE_VALUE) 0 else 1
    }

    private fun Int.toStorageEnvType(): String {
        return if (this == 0) NO_ENV_STORAGE_VALUE else WITH_ENV_STORAGE_VALUE
    }

    private fun String.toRuntimeType(): Int {
        return trim().toIntOrNull() ?: 0
    }

    private fun Boolean.toInt(): Int = if (this) 1 else 0

    private data class StoredUserConfigDraft(
        val draft: UserHookConfigDraft,
        val createdAtMillis: Long,
    )

    private companion object {
        const val NO_ENV_STORAGE_VALUE = "no_env"
        const val WITH_ENV_STORAGE_VALUE = "with_env"
        const val CUSTOM_HOOK_TYPE = "0"
        const val RUNTIME_DATABASE_NAME = "killer_hook.db"
        const val APP_HOOK_TABLE = "app_hook_config"
        const val CHANGE_VALUE_TABLE = "change_value_config"
        const val FRIDA_SCRIPTS_TABLE = "frida_scripts"
    }
}
