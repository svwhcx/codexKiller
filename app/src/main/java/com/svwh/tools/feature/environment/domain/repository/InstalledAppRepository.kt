package com.svwh.tools.feature.environment.domain.repository

import com.svwh.tools.feature.environment.domain.model.InstalledAppItem

interface InstalledAppRepository {
    suspend fun getInstalledApps(
        showSystemApps: Boolean,
        hookedPackages: Set<String>,
    ): List<InstalledAppItem>
}
