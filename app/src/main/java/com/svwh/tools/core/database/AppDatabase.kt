package com.svwh.tools.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.svwh.tools.core.database.dao.HookStateDao
import com.svwh.tools.core.database.dao.ToolHistoryDao
import com.svwh.tools.core.database.entity.HookStateEntity
import com.svwh.tools.core.database.entity.ToolHistoryEntity

@Database(
    entities = [
        HookStateEntity::class,
        ToolHistoryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hookStateDao(): HookStateDao
    abstract fun toolHistoryDao(): ToolHistoryDao
}
