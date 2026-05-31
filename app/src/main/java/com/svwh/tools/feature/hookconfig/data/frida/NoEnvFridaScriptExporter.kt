package com.svwh.tools.feature.hookconfig.data.frida

import android.os.Environment
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem
import com.svwh.tools.feature.hookconfig.domain.model.GlobalFridaScriptScope
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoEnvFridaScriptExporter @Inject constructor() {
    fun syncPackage(
        envType: String,
        packageName: String,
        scripts: List<FridaScriptItem>,
    ) {
        if (packageName.isBlank() ||
            envType != NO_ENV_STORAGE_VALUE ||
            packageName == GlobalFridaScriptScope.PACKAGE_NAME
        ) {
            return
        }

        val scriptDir = runtimeScriptDirectory(packageName)
        ensureDirectory(scriptDir)

        scriptDir.listFiles()
            ?.filter { file -> file.isFile && file.extension.equals("js", ignoreCase = true) }
            ?.forEach { file -> file.delete() }

        scripts
            .filter { script -> script.enabled && script.scriptContent.isNotBlank() }
            .forEach { script ->
                File(scriptDir, script.runtimeFileName()).writeText(script.scriptContent)
            }
    }

    private fun ensureDirectory(directory: File) {
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Cannot create Frida script directory: ${directory.absolutePath}")
        }
        if (!directory.isDirectory) {
            throw IOException("Frida script path is not a directory: ${directory.absolutePath}")
        }
    }

    private fun runtimeScriptDirectory(packageName: String): File {
        return File(
            Environment.getExternalStorageDirectory(),
            "Android/media/$packageName/frida/noenv",
        )
    }

    private fun FridaScriptItem.runtimeFileName(): String {
        val safeName = name
            .trim()
            .replace(Regex("""[^A-Za-z0-9._-]"""), "_")
            .ifBlank { "script" }
            .removeSuffix(".js")
        return "${id}_$safeName.js"
    }

    private companion object {
        const val NO_ENV_STORAGE_VALUE = "no_env"
    }
}
