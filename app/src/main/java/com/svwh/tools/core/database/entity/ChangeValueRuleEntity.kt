package com.svwh.tools.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "change_value_rules",
    foreignKeys = [
        ForeignKey(
            entity = UserHookConfigEntity::class,
            parentColumns = ["id"],
            childColumns = ["hookConfigId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["hookConfigId"]),
    ],
)
data class ChangeValueRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hookConfigId: Long,
    val rule: String,
    val paramNumber: Int,
    val matchValue: String,
    val replaceValue: String,
)
