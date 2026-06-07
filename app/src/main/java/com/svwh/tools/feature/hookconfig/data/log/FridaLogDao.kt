package com.svwh.tools.feature.hookconfig.data.log

import androidx.room.Dao
import androidx.room.Query

@Dao
interface FridaLogDao {
    @Query(
        """
        SELECT * FROM logs
        WHERE (:levelCount = 0 OR UPPER(level) IN (:levels))
        AND (
            :keyword = ''
            OR script LIKE '%' || :keyword || '%'
            OR msg LIKE '%' || :keyword || '%'
        )
        ORDER BY id DESC
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun queryLogs(
        levels: List<String>,
        levelCount: Int,
        keyword: String,
        limit: Int,
        offset: Int,
    ): List<FridaLogEntity>

    @Query("SELECT * FROM logs WHERE id = :id LIMIT 1")
    suspend fun queryLogDetail(id: Long): FridaLogEntity?

    @Query("DELETE FROM logs")
    suspend fun deleteAll()

    @Query("DELETE FROM logs WHERE UPPER(level) IN (:levels)")
    suspend fun deleteByLevels(levels: List<String>)

    @Query("DELETE FROM logs WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
