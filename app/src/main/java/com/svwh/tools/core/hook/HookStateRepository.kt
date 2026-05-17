package com.svwh.tools.core.hook

interface HookStateRepository {
    suspend fun syncAndGetEnabledPackages(
        environmentType: HookEnvironmentType,
        packageNames: Set<String>,
    ): Set<String>

    suspend fun setHookEnabled(
        packageName: String,
        environmentType: HookEnvironmentType,
        enabled: Boolean,
    ): Boolean
}
