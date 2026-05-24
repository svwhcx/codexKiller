package com.svwh.tools.core.navigation

import android.net.Uri

object AppDestination {
    const val MAIN = "main"
    const val REPACK_APP_LIST = "repack_app_list"
    const val HOOK_CONFIG_ROUTE = "hook_config/{envType}/{packageName}?appName={appName}"
    const val USER_HOOK_CONFIG_EDITOR_ROUTE =
        "user_hook_config_editor/{envType}/{packageName}/{configId}?appName={appName}"

    fun hookConfigRoute(
        envType: String,
        packageName: String,
        appName: String,
    ): String {
        return "hook_config/${Uri.encode(envType)}/${Uri.encode(packageName)}?appName=${Uri.encode(appName)}"
    }

    fun userHookConfigEditorRoute(
        envType: String,
        packageName: String,
        configId: Long,
        appName: String,
    ): String {
        return "user_hook_config_editor/${Uri.encode(envType)}/${Uri.encode(packageName)}/${configId}?appName=${Uri.encode(appName)}"
    }
}
