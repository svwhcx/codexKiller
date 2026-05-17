package com.svwh.tools.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.svwh.tools.core.database.entity.HookStateEntity

@Dao
interface HookStateDao {
    @Query(
        """
        SELECT * FROM hook_states
        WHERE envType = :envType AND packageName IN (:packageNames)
        """,
    )
    suspend fun getByPackages(
        envType: String,
        packageNames: Set<String>,
    ): List<HookStateEntity>

    @Query(
        """
        SELECT * FROM hook_states
        WHERE envType = :envType AND packageName = :packageName
        LIMIT 1
        """,
    )
    suspend fun getByPackage(
        envType: String,
        packageName: String,
    ): HookStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(states: List<HookStateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: HookStateEntity)

    @Transaction
    suspend fun upsertSyncedStates(states: List<HookStateEntity>) {
        if (states.isNotEmpty()) {
            insertAll(states)
        }
    }
}
