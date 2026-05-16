package com.svwh.tools.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_history")
data class ToolHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val toolId: String,
    val title: String,
    val createdAtMillis: Long,
)
