package com.svwh.tools.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_hook_configs",
    indices = [
        Index(value = ["packageName", "envType"]),
    ],
)
data class UserHookConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val envType: String,
    val configName: String,
    val className: String,
    val methodName: String,
    val params: String,
    val methodSignature: String = "",
    val invokeClass: ByteArray = byteArrayOf(),
    val hookStatus: Boolean = true,
    val isLog: Boolean = true,
    val isInterrupted: Boolean = false,
    val enabled: Boolean = true,
    val exp: String = "",
    val type: String = "",
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
