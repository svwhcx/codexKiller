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

        val runtimeRootDir = runtimeRootDirectory(packageName)
        val runtimeScriptDir = runtimeScriptDirectory(packageName)
        ensureDirectory(runtimeRootDir)
        ensureDirectory(runtimeScriptDir)

        runtimeScriptDir.listFiles()
            ?.filter { file -> file.isFile && file.extension.equals("js", ignoreCase = true) }
            ?.forEach { file -> file.delete() }

        val enabledScripts = scripts
            .filter { script -> script.enabled && script.scriptContent.isNotBlank() }

        enabledScripts.forEach { script ->
            File(runtimeScriptDir, script.runtimeFileName()).writeText(
                script.runtimeScriptContent(),
                Charsets.UTF_8,
            )
        }

        File(runtimeRootDir, SCRIPT_PATH_FILE_NAME).writeText(
            enabledScripts.joinToString(separator = "\n") { script ->
                File(runtimeScriptDir, script.runtimeFileName()).absolutePath
            },
            Charsets.UTF_8,
        )

        if (enabledScripts.isNotEmpty()) {
            ensureKillerScript(runtimeRootDir, runtimeScriptDir)
        }
    }

    private fun ensureKillerScript(
        runtimeRootDir: File,
        runtimeScriptDir: File,
    ) {
        val killerScriptFile = File(runtimeRootDir, KILLER_SCRIPT_FILE_NAME)
        if (killerScriptFile.exists()) return

        val killerScriptBody = context.assets
            .open(KILLER_SCRIPT_ASSET_PATH)
            .bufferedReader(Charsets.UTF_8)
            .use { reader -> reader.readText() }

        killerScriptFile.writeText(
            buildString {
                append("const _SCRIPT_PATH_ = '")
                append(runtimeScriptDir.absolutePath)
                append("';")
                append('\n')
                append(killerScriptBody.trimStart())
            },
            Charsets.UTF_8,
        )
    }

    private fun ensureDirectory(directory: File) {
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Cannot create Frida script directory: ${directory.absolutePath}")
        }
        if (!directory.isDirectory) {
            throw IOException("Frida script path is not a directory: ${directory.absolutePath}")
        }
    }

    private fun runtimeRootDirectory(packageName: String): File {
        return File(
            Environment.getExternalStorageDirectory(),
            "Android/media/$packageName/frida/noenv",
        )
    }

    private fun runtimeScriptDirectory(packageName: String): File {
        return File(runtimeRootDirectory(packageName), SCRIPTS_DIRECTORY_NAME)
    }

    private fun FridaScriptItem.runtimeFileName(): String {
        return "${name.trim()}.js"
    }

    private fun FridaScriptItem.runtimeScriptContent(): String {
        return buildString {
//            append(bridgeScript.trimEnd())
//            append('\n')
//            append('\n')
            append(scriptContent)
        }
    }

    private companion object {
        const val NO_ENV_STORAGE_VALUE = "no_env"
        const val BRIDGE_JAVA_ASSET_PATH = "conf/bridge-java.js"
        const val KILLER_SCRIPT_ASSET_PATH = "conf/killer-frida.js"
        const val KILLER_SCRIPT_FILE_NAME = "killer-frida.js"
        const val SCRIPT_PATH_FILE_NAME = "sPath"
        const val SCRIPTS_DIRECTORY_NAME = "scripts"
    }
}
