package com.svwh.tools.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.svwh.tools.core.database.entity.ToolHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolHistoryDao {
    @Query("SELECT * FROM tool_history ORDER BY createdAtMillis DESC")
    fun observeHistory(): Flow<List<ToolHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entity: ToolHistoryEntity)

    @Query("DELETE FROM tool_history")
    suspend fun clearHistory()
}
