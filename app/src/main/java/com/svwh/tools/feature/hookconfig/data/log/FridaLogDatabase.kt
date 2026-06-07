package com.svwh.tools.feature.hookconfig.data.log

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FridaLogEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FridaLogDatabase : RoomDatabase() {
    abstract fun fridaLogDao(): FridaLogDao
}
