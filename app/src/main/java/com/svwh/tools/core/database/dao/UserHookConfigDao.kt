package com.svwh.tools.core.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import com.svwh.tools.core.database.entity.ChangeValueRuleEntity
import com.svwh.tools.core.database.entity.UserHookConfigEntity

data class UserHookConfigWithRules(
    @Embedded
    val config: UserHookConfigEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "hookConfigId",
    )
    val rules: List<ChangeValueRuleEntity>,
)

@Dao
interface UserHookConfigDao {
    @Transaction
    @Query(
        """
        SELECT * FROM user_hook_configs
        WHERE packageName = :packageName
          AND envType = :envType
        ORDER BY updatedAtMillis DESC, id DESC
        """,
    )
    suspend fun getConfigsWithRules(
        packageName: String,
        envType: String,
    ): List<UserHookConfigWithRules>

    @Transaction
    @Query(
        """
        SELECT * FROM user_hook_configs
        WHERE id = :id
        LIMIT 1
        """,
    )
    suspend fun getConfigWithRulesById(id: Long): UserHookConfigWithRules?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(entity: UserHookConfigEntity): Long

    @Update
    suspend fun updateConfig(entity: UserHookConfigEntity)

    @Query("DELETE FROM user_hook_configs WHERE id IN (:ids)")
    suspend fun deleteConfigsByIds(ids: List<Long>)

    @Query("UPDATE user_hook_configs SET enabled = :enabled, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateEnabled(
        id: Long,
        enabled: Boolean,
        updatedAtMillis: Long,
    )

    @Query(
        """
        SELECT COUNT(1) FROM user_hook_configs
        WHERE packageName = :packageName
          AND envType = :envType
          AND configName = :configName
          AND (:excludeId <= 0 OR id != :excludeId)
        """,
    )
    suspend fun countConfigName(
        packageName: String,
        envType: String,
        configName: String,
        excludeId: Long,
    ): Int

    @Query(
        """
        SELECT COUNT(1) FROM user_hook_configs
        WHERE packageName = :packageName
          AND envType = :envType
          AND className = :className
          AND methodName = :methodName
          AND params = :params
          AND (:excludeId <= 0 OR id != :excludeId)
        """,
    )
    suspend fun countSignature(
        packageName: String,
        envType: String,
        className: String,
        methodName: String,
        params: String,
        excludeId: Long,
    ): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<ChangeValueRuleEntity>)

    @Query("DELETE FROM change_value_rules WHERE hookConfigId = :hookConfigId")
    suspend fun deleteRulesByHookConfigId(hookConfigId: Long)

    @Query("DELETE FROM change_value_rules WHERE hookConfigId IN (:hookConfigIds)")
    suspend fun deleteRulesByHookConfigIds(hookConfigIds: List<Long>)
}
