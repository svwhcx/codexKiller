package com.svwh.tools.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.svwh.tools.core.database.dao.HookStateDao
import com.svwh.tools.core.database.dao.ToolHistoryDao
import com.svwh.tools.core.database.dao.UserHookConfigDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private val Migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `hook_states` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `packageName` TEXT NOT NULL,
                    `envType` TEXT NOT NULL,
                    `enabled` INTEGER NOT NULL,
                    `statusFilePath` TEXT NOT NULL,
                    `source` TEXT NOT NULL,
                    `note` TEXT,
                    `createdAtMillis` INTEGER NOT NULL,
                    `updatedAtMillis` INTEGER NOT NULL,
                    `lastSyncedAtMillis` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE UNIQUE INDEX IF NOT EXISTS
                `index_hook_states_packageName_envType`
                ON `hook_states` (`packageName`, `envType`)
                """.trimIndent(),
            )
        }
    }

    private val Migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `user_hook_configs` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `packageName` TEXT NOT NULL,
                    `envType` TEXT NOT NULL,
                    `configName` TEXT NOT NULL,
                    `className` TEXT NOT NULL,
                    `methodName` TEXT NOT NULL,
                    `params` TEXT NOT NULL,
                    `methodSignature` TEXT NOT NULL,
                    `invokeClass` BLOB NOT NULL,
                    `hookStatus` INTEGER NOT NULL,
                    `isLog` INTEGER NOT NULL,
                    `isInterrupted` INTEGER NOT NULL,
                    `enabled` INTEGER NOT NULL,
                    `exp` TEXT NOT NULL,
                    `type` TEXT NOT NULL,
                    `createdAtMillis` INTEGER NOT NULL,
                    `updatedAtMillis` INTEGER NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS
                `index_user_hook_configs_packageName_envType`
                ON `user_hook_configs` (`packageName`, `envType`)
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `change_value_rules` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `hookConfigId` INTEGER NOT NULL,
                    `rule` TEXT NOT NULL,
                    `paramNumber` INTEGER NOT NULL,
                    `matchValue` TEXT NOT NULL,
                    `replaceValue` TEXT NOT NULL,
                    FOREIGN KEY(`hookConfigId`) REFERENCES `user_hook_configs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS
                `index_change_value_rules_hookConfigId`
                ON `change_value_rules` (`hookConfigId`)
                """.trimIndent(),
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "stool.db",
        )
            .addMigrations(Migration1To2, Migration2To3)
            .build()
    }

    @Provides
    fun provideHookStateDao(database: AppDatabase): HookStateDao {
        return database.hookStateDao()
    }

    @Provides
    fun provideToolHistoryDao(database: AppDatabase): ToolHistoryDao {
        return database.toolHistoryDao()
    }

    @Provides
    fun provideUserHookConfigDao(database: AppDatabase): UserHookConfigDao {
        return database.userHookConfigDao()
    }
}
