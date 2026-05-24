package com.svwh.tools.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "frida_scripts",
    indices = [
        Index(value = ["packageName", "envType"]),
    ],
)
data class FridaScriptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val envType: String,
    val name: String,
    val scriptContent: String,
    val enabled: Boolean = true,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
