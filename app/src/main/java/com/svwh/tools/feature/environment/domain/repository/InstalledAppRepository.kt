package com.svwh.tools.feature.environment.domain.repository

import com.svwh.tools.feature.environment.domain.model.InstalledAppItem

data class InstalledAppMetaDataFilter(
    val name: String,
    val value: String? = null,
)

interface InstalledAppRepository {
    suspend fun getInstalledApps(
        showSystemApps: Boolean,
        hookedPackages: Set<String>,
        requiredMetaData: InstalledAppMetaDataFilter? = null,
    ): List<InstalledAppItem>
}
