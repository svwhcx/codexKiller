package com.svwh.tools.feature.hookconfig.data.log

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Environment
import com.svwh.tools.core.common.AppError
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.data.HookLogTypeRegistry
import com.svwh.tools.feature.hookconfig.domain.model.HookLogPageResult
import com.svwh.tools.feature.hookconfig.domain.model.HookLogQuery
import com.svwh.tools.feature.hookconfig.domain.model.HookLogRecord
import com.svwh.tools.feature.hookconfig.domain.model.HookLogTypeOption
import com.svwh.tools.feature.hookconfig.domain.repository.HookLogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ContentProviderHookLogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val typeRegistry: HookLogTypeRegistry,
) : HookLogRepository {

    override suspend fun queryLogs(query: HookLogQuery): AppResult<HookLogPageResult> = withContext(Dispatchers.IO) {
        runCatching {
            val logs = when (query.envType) {
                "with_env" -> queryWithEnvLogs(query)
                "no_env" -> queryNoEnvLogs(query)
                else -> emptyList()
            }
            AppResult.Success(
                HookLogPageResult(
                    items = logs,
                    hasMore = logs.size >= query.pageSize,
                )
            )
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun queryLogDetail(
        envType: String,
        packageName: String,
        id: Long,
    ): AppResult<HookLogRecord?> = withContext(Dispatchers.IO) {
        runCatching {
            when (envType) {
                "with_env" -> queryWithEnvDetail(packageName, id)
                "no_env" -> queryNoEnvDetail(packageName, id)
                else -> null
            }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Failure(AppError.Unknown(it)) },
        )
    }

    override suspend fun deleteAll(
        envType: String,
        packageName: String,
        selectedTypes: Set<Int>,
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            when (envType) {
                "with_env" -> deleteWithEnvLogs(packageName, selectedTypes)
                "no_env" -> deleteNoEnvLogs(packageName, selectedTypes)
            }
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    override suspend fun queryAvailableTypes(
        envType: String,
        packageName: String,
    ): AppResult<List<HookLogTypeOption>> = withContext(Dispatchers.IO) {
        runCatching {
            AppResult.Success(typeRegistry.options())
        }.getOrElse { AppResult.Failure(AppError.Unknown(it)) }
    }

    private fun queryWithEnvLogs(query: HookLogQuery): List<HookLogRecord> {
        val dbFile = withEnvLogDatabaseFile(query.packageName)
        if (!dbFile.exists()) return emptyList()

        return SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            val typeSelection = buildTypeSelection(query.selectedTypes)
            val whereParts = mutableListOf<String>()
            val args = mutableListOf<String>()
            if (typeSelection.isNotEmpty()) {
                whereParts += "type IN (${typeSelection.joinToString(",") { "?" }})"
                args += typeSelection
            }
            if (query.keyword.isNotBlank()) {
                whereParts += "(title LIKE ? OR content LIKE ? OR stackTrace LIKE ?)"
                repeat(3) { args += "%${query.keyword}%" }
            }

            db.query(
                "HookLog",
                arrayOf("id", "title", "time", "type", "packageName", "content", "stackTrace", "status", "exp", "isRead"),
                whereParts.takeIf { it.isNotEmpty() }?.joinToString(" AND "),
                args.toTypedArray(),
                null,
                null,
                "id DESC",
                "${query.offset},${query.pageSize}",
            ).useRecords(fallbackPackageName = query.packageName)
        }
    }

    private fun queryNoEnvLogs(query: HookLogQuery): List<HookLogRecord> {
        val uri = Uri.parse("content://${query.packageName}.killer_hook_provider")
        val args = mutableListOf("log_offset", query.offset.toString(), query.pageSize.toString())
        args += buildTypeSelection(query.selectedTypes)
        if (query.keyword.isNotBlank()) {
            args += query.keyword
        }
        val cursor = context.contentResolver.query(
            uri,
            args.toTypedArray(),
            null,
            null,
            null,
        )
        return cursor.useRecords(fallbackPackageName = query.packageName)
            .filterByKeyword(query.keyword)
    }

    private fun queryWithEnvDetail(packageName: String, id: Long): HookLogRecord? {
        val dbFile = withEnvLogDatabaseFile(packageName)
        if (!dbFile.exists()) return null

        return SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            val record = db.query(
                "HookLog",
                null,
                "id = ?",
                arrayOf(id.toString()),
                null,
                null,
                null,
                "1",
            ).useRecords(fallbackPackageName = packageName).firstOrNull()
            db.execSQL("UPDATE HookLog SET isRead = 1 WHERE id = ?", arrayOf(id))
            record
        }
    }

    private fun queryNoEnvDetail(packageName: String, id: Long): HookLogRecord? {
        val uri = Uri.parse("content://${packageName}.killer_hook_provider")
        val cursor = context.contentResolver.query(
            uri,
            arrayOf("log_detail", id.toString()),
            null,
            null,
            null,
        )
        return cursor.useRecords(fallbackPackageName = packageName).firstOrNull()
    }

    private fun deleteWithEnvLogs(packageName: String, selectedTypes: Set<Int>) {
        val dbFile = withEnvLogDatabaseFile(packageName)
        if (!dbFile.exists()) return

        SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            if (selectedTypes.isEmpty()) {
                db.delete("HookLog", null, null)
            } else {
                val typeSelection = selectedTypes.map(Int::toString)
                db.delete(
                    "HookLog",
                    "type IN (${typeSelection.joinToString(",") { "?" }})",
                    typeSelection.toTypedArray(),
                )
            }
        }
    }

    private fun deleteNoEnvLogs(packageName: String, selectedTypes: Set<Int>) {
        val uri = Uri.parse("content://${packageName}.killer_hook_provider")
        val args = mutableListOf(
            if (selectedTypes.isEmpty()) "delete_all" else "delete_type_all"
        )
        args += selectedTypes.map(Int::toString)
        context.contentResolver.query(uri, args.toTypedArray(), null, null, null)?.close()
    }

    private fun buildTypeSelection(selectedTypes: Set<Int>): List<String> {
        return if (selectedTypes.isEmpty()) {
            typeRegistry.options().map { it.type.toString() }
        } else {
            selectedTypes.map(Int::toString)
        }
    }

    private fun withEnvLogDatabaseFile(packageName: String): File {
        return File(
            Environment.getExternalStorageDirectory(),
            "Android/media/$packageName/killer_hook_no_env.db",
        )
    }

    private fun Cursor?.useRecords(fallbackPackageName: String = ""): List<HookLogRecord> {
        if (this == null) return emptyList()
        return use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        HookLogRecord(
                            id = cursor.longValue("id"),
                            title = cursor.stringValue("title"),
                            time = cursor.stringValue("time"),
                            type = cursor.intValue("type"),
                            typeLabel = typeRegistry.titleOf(cursor.intValue("type")),
                            packageName = cursor.stringValue("packageName").ifBlank { fallbackPackageName },
                            content = cursor.stringValue("content"),
                            stackTrace = cursor.stringValue("stackTrace"),
                            status = cursor.intValueOrNull("status"),
                            exp = cursor.stringValueOrNull("exp"),
                            isRead = cursor.intValueOrNull("isRead") == 1,
                        )
                    )
                }
            }
        }
    }

    private fun List<HookLogRecord>.filterByKeyword(keyword: String): List<HookLogRecord> {
        if (keyword.isBlank()) return this
        return filter { record ->
            record.title.contains(keyword, ignoreCase = true) ||
                record.content.contains(keyword, ignoreCase = true) ||
                record.stackTrace.contains(keyword, ignoreCase = true)
        }
    }

    private fun Cursor.stringValue(columnName: String): String {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getString(index).orEmpty() else ""
    }

    private fun Cursor.stringValueOrNull(columnName: String): String? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getString(index) else null
    }

    private fun Cursor.intValue(columnName: String): Int {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getInt(index) else 0
    }

    private fun Cursor.intValueOrNull(columnName: String): Int? {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getInt(index) else null
    }

    private fun Cursor.longValue(columnName: String): Long {
        val index = getColumnIndex(columnName)
        return if (index >= 0 && !isNull(index)) getLong(index) else 0L
    }
}
