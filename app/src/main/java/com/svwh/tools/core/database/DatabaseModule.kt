package com.svwh.tools.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.svwh.tools.core.database.dao.HookStateDao
import com.svwh.tools.core.database.dao.ToolHistoryDao
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
            .addMigrations(Migration1To2)
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
}
