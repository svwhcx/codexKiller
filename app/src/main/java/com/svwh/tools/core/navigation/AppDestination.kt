package com.svwh.tools.core.navigation

import android.net.Uri

object AppDestination {
    const val MAIN = "main"
    const val REPACK_APP_LIST = "repack_app_list"
    const val EXTRA_NAV_DESTINATION = "extra_nav_destination"
    const val GLOBAL_FRIDA_SCRIPTS = "global_frida_scripts"
    const val HOOK_CONFIG_ROUTE = "hook_config/{envType}/{packageName}?appName={appName}"
    const val USER_HOOK_CONFIG_EDITOR_ROUTE =
        "user_hook_config_editor/{envType}/{packageName}/{configId}?appName={appName}"
    const val FRIDA_SCRIPT_EDITOR_ROUTE =
        "frida_script_editor/{envType}/{packageName}/{scriptId}?appName={appName}"
    const val FRIDA_LOG_DETAIL_ROUTE =
        "frida_log_detail/{packageName}/{logId}"

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

    fun fridaScriptEditorRoute(
        envType: String,
        packageName: String,
        scriptId: Long,
        appName: String,
    ): String {
        return "frida_script_editor/${Uri.encode(envType)}/${Uri.encode(packageName)}/${scriptId}?appName=${Uri.encode(appName)}"
    }

    fun fridaLogDetailRoute(
        packageName: String,
        logId: Long,
    ): String {
        return "frida_log_detail/${Uri.encode(packageName)}/${logId}"
    }
}
