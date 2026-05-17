package com.svwh.tools.core.permission

data class PermissionRequest(
    val permission: String,
    val rationale: String,
)

enum class PermissionStatus {
    Granted,
    Denied,
}

enum class PackageVisibilityAccess {
    LegacyNoRuntimePermission,
    QueryAllPackagesDeclared,
    LimitedVisibility,
}

sealed interface ExternalStorageAccessStatus {
    data object Granted : ExternalStorageAccessStatus
    data class RuntimePermissionRequired(val permission: String) : ExternalStorageAccessStatus
    data object AllFilesAccessRequired : ExternalStorageAccessStatus
}
