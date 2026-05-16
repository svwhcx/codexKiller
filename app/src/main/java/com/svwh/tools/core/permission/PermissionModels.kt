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
