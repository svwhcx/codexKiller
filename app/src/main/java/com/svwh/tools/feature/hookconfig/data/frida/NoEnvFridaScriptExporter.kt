package com.svwh.tools.feature.hookconfig.data.frida

import android.content.Context
import android.os.Environment
import com.svwh.tools.feature.hookconfig.domain.model.FridaScriptItem
import com.svwh.tools.feature.hookconfig.domain.model.GlobalFridaScriptScope
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoEnvFridaScriptExporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val bridgeScript: String by lazy {
        context.assets
            .open(BRIDGE_JAVA_ASSET_PATH)
            .bufferedReader(Charsets.UTF_8)
            .use { reader -> reader.readText() }
    }

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
                File(scriptDir, script.runtimeFileName()).writeText(script.runtimeScriptContent(), Charsets.UTF_8)
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
        return "$name.js"
    }

    private fun FridaScriptItem.runtimeScriptContent(): String {
        return buildString {
            append(bridgeScript.trimEnd())
            append('\n')
                .append('\n')
            append(scriptContent)
        }
    }

    private companion object {
        const val NO_ENV_STORAGE_VALUE = "no_env"
        const val BRIDGE_JAVA_ASSET_PATH = "conf/bridge-java.js"
    }
}
