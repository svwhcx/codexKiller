package com.svwh.tools.core.navigation

import android.net.Uri

object AppDestination {
    const val MAIN = "main"
    const val REPACK_APP_LIST = "repack_app_list"
    const val HOOK_CONFIG_ROUTE = "hook_config/{envType}/{packageName}?appName={appName}"

    fun hookConfigRoute(
        envType: String,
        packageName: String,
        appName: String,
    ): String {
        return "hook_config/${Uri.encode(envType)}/${Uri.encode(packageName)}?appName=${Uri.encode(appName)}"
    }
}
