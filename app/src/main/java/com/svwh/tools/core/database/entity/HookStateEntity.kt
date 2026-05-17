package com.svwh.tools.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hook_states",
    indices = [
        Index(
            value = ["packageName", "envType"],
            unique = true,
        ),
    ],
)
data class HookStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val envType: String,
    val enabled: Boolean,
    val statusFilePath: String,
    val source: String = "media_file",
    val note: String? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val lastSyncedAtMillis: Long,
)
