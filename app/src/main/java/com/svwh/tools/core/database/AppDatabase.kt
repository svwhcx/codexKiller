package com.svwh.tools.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.svwh.tools.core.database.dao.HookStateDao
import com.svwh.tools.core.database.dao.UserHookConfigDao
import com.svwh.tools.core.database.dao.ToolHistoryDao
import com.svwh.tools.core.database.entity.ChangeValueRuleEntity
import com.svwh.tools.core.database.entity.HookStateEntity
import com.svwh.tools.core.database.entity.ToolHistoryEntity
import com.svwh.tools.core.database.entity.UserHookConfigEntity

@Database(
    entities = [
        HookStateEntity::class,
        ToolHistoryEntity::class,
        UserHookConfigEntity::class,
        ChangeValueRuleEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hookStateDao(): HookStateDao
    abstract fun toolHistoryDao(): ToolHistoryDao
    abstract fun userHookConfigDao(): UserHookConfigDao
}
