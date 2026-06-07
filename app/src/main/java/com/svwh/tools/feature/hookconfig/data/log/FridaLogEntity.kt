package com.svwh.tools.feature.hookconfig.data.log

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "logs",
    indices = [
        Index(value = ["env"], name = "idx_logs_env"),
        Index(value = ["level"], name = "idx_logs_level"),
        Index(value = ["script"], name = "idx_logs_script"),
        Index(value = ["ts"], name = "idx_logs_ts", orders = [Index.Order.DESC]),
    ],
)
data class FridaLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long? = null,
    @ColumnInfo(name = "ts")
    val ts: Long? = null,
    @ColumnInfo(name = "level")
    val level: String? = null,
    @ColumnInfo(name = "script")
    val script: String? = null,
    @ColumnInfo(name = "msg")
    val msg: String? = null,
    @ColumnInfo(name = "env")
    val env: String? = null,
)
