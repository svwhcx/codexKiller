package com.svwh.tools.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.svwh.tools.core.database.entity.FridaScriptEntity

@Dao
interface FridaScriptDao {
    @Query(
        """
        SELECT * FROM frida_scripts
        WHERE packageName = :packageName
          AND envType = :envType
        ORDER BY updatedAtMillis DESC, id DESC
        """,
    )
    suspend fun getScripts(
        packageName: String,
        envType: String,
    ): List<FridaScriptEntity>

    @Query("SELECT * FROM frida_scripts WHERE id = :id LIMIT 1")
    suspend fun getScriptById(id: Long): FridaScriptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(entity: FridaScriptEntity): Long

    @Update
    suspend fun updateScript(entity: FridaScriptEntity)

    @Query("UPDATE frida_scripts SET enabled = :enabled, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateEnabled(
        id: Long,
        enabled: Boolean,
        updatedAtMillis: Long,
    )

    @Query("DELETE FROM frida_scripts WHERE id IN (:ids)")
    suspend fun deleteScriptsByIds(ids: List<Long>)

    @Query(
        """
        SELECT COUNT(1) FROM frida_scripts
        WHERE packageName = :packageName
          AND envType = :envType
          AND name = :name
          AND (:excludeId <= 0 OR id != :excludeId)
        """,
    )
    suspend fun countScriptName(
        packageName: String,
        envType: String,
        name: String,
        excludeId: Long,
    ): Int
}
