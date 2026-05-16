package com.svwh.tools.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.svwh.tools.core.database.dao.ToolHistoryDao
import com.svwh.tools.core.database.entity.ToolHistoryEntity

@Database(
    entities = [
        ToolHistoryEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun toolHistoryDao(): ToolHistoryDao
}
