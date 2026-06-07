package com.svwh.tools.feature.hookconfig.data.log

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.room.Room
import com.svwh.tools.core.common.AppError
import com.svwh.tools.core.common.AppResult
import com.svwh.tools.feature.hookconfig.domain.model.HookLogPageResult
import com.svwh.tools.feature.hookconfig.domain.model.HookLogQuery
import com.svwh.tools.feature.hookconfig.domain.model.HookLogRecord
import com.svwh.tools.feature.hookconfig.domain.model.HookLogTypeOption
import com.svwh.tools.feature.hookconfig.domain.repository.FridaLogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class RoomFridaLogRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : FridaLogRepository {
    override suspend fun queryLogs(query: HookLogQuery): AppResult<HookLogPageResult> = withContext(Dispatchers.IO) {
        runCatching {
            val db = fridaLogDatabase(query.packageName) ?: return@runCatching HookLogPageResult(emptyList(), false)
            try {
                val levels = query.selectedTypes.mapNotNull(::levelNameOf)
                val logs = db.fridaLogDao().queryLogs(
                    levels = levels,
                    levelCount = levels.size,
                    keyword = query.keyword,
                    limit = query.pageSize,
                    offset = query.offset,
                ).map { it.toRecord(query.packageName) }
                HookLogPageResult(
                    items = logs,
                    hasMore = logs.size >= query.pageSize,
                )
            } finally {
                db.close()
            }
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure =  {
                Log.e("错误:", it.toString())
                AppResult.Failure(AppError.Unknown(it)) },
        )
    }

    override suspend fun queryLogDetail(packageName: String, id: Long): AppResult<HookLogRecord?> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = fridaLogDatabase(packageName) ?: return@runCatching null
                try {
                    db.fridaLogDao()
                        .queryLogDetail(id)
                        ?.toRecord(packageName)
                } finally {
                    db.close()
                }
            }.fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = { AppResult.Failure(AppError.Unknown(it)) },
            )
        }

    override suspend fun deleteAll(packageName: String, selectedLevels: Set<Int>): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val db = fridaLogDatabase(packageName) ?: return@runCatching
                try {
                    val dao = db.fridaLogDao()
                    val levels = selectedLevels.mapNotNull(::levelNameOf)
                    if (levels.isEmpty()) {
                        dao.deleteAll()
                    } else {
                        dao.deleteByLevels(levels)
                    }
                } finally {
                    db.close()
                }
            }.fold(
                onSuccess = { AppResult.Success(Unit) },
                onFailure = { AppResult.Failure(AppError.Unknown(it)) },
            )
        }

    override suspend fun deleteByIds(packageName: String, ids: Set<Long>): AppResult<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            if (ids.isEmpty()) return@runCatching
            val db = fridaLogDatabase(packageName) ?: return@runCatching
            try {
                db.fridaLogDao().deleteByIds(ids.toList())
            } finally {
                db.close()
            }
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { AppResult.Failure(AppError.Unknown(it)) },
        )
    }

    override suspend fun queryAvailableLevels(): AppResult<List<HookLogTypeOption>> = withContext(Dispatchers.IO) {
        AppResult.Success(LEVEL_OPTIONS)
    }

    private fun fridaLogDatabase(packageName: String): FridaLogDatabase? {
        val dbFile = fridaLogDatabaseFile(packageName) ?: return null
        return Room.databaseBuilder(
            context,
            FridaLogDatabase::class.java,
            dbFile.absolutePath,
        )
            .build()
    }

    private fun fridaLogDatabaseFile(packageName: String): File? {
        if (packageName.isBlank()) return null
        val root = Environment.getExternalStorageDirectory()
        return listOf(
            "Android/media/$packageName/frida/frida_log.db"
        )
            .map { File(root, it) }
            .firstOrNull { it.exists() && it.isFile }
    }

    private fun FridaLogEntity.toRecord(packageName: String): HookLogRecord {
        val normalizedLevel = level.orEmpty().uppercase(Locale.US).ifBlank { "INFO" }
        return HookLogRecord(
            id = id ?: 0L,
            title = script.orEmpty().ifBlank { normalizedLevel },
            time = formatTimestamp(ts ?: 0L),
            type = levelTypeOf(normalizedLevel),
            typeLabel = normalizedLevel,
            packageName = packageName,
            content = msg.orEmpty(),
            stackTrace = "",
            status = if (normalizedLevel == "ERROR") 1 else 0,
            exp = env?.takeIf { it.isNotBlank() },
            isRead = true,
        )
    }

    private fun formatTimestamp(value: Long): String {
        if (value <= 0L) return ""
        val millis = if (value < 1_000_000_000_000L) value * 1000L else value
        return DATE_FORMAT.get().format(Date(millis))
    }

    private companion object {
        const val LEVEL_INFO = 1
        const val LEVEL_WARN = 2
        const val LEVEL_ERROR = 3

        val LEVEL_OPTIONS = listOf(
            HookLogTypeOption(LEVEL_INFO, "INFO"),
            HookLogTypeOption(LEVEL_WARN, "WARN"),
            HookLogTypeOption(LEVEL_ERROR, "ERROR"),
        )

        val DATE_FORMAT = ThreadLocal.withInitial {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        }

        fun levelNameOf(type: Int): String? {
            return when (type) {
                LEVEL_INFO -> "INFO"
                LEVEL_WARN -> "WARN"
                LEVEL_ERROR -> "ERROR"
                else -> null
            }
        }

        fun levelTypeOf(level: String): Int {
            return when (level) {
                "WARN", "WARNING" -> LEVEL_WARN
                "ERROR" -> LEVEL_ERROR
                else -> LEVEL_INFO
            }
        }
    }
}
