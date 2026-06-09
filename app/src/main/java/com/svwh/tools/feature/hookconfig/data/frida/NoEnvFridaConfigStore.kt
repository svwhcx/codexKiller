package com.svwh.tools.feature.hookconfig.data.frida

import android.os.Environment
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class NoEnvFridaConfigStore @Inject constructor() {
    suspend fun readDelayInjectMillis(
        envType: String,
        packageName: String,
    ): Long? = withContext(Dispatchers.IO) {
        if (!isNoEnvPackage(envType, packageName)) return@withContext null
        val configFile = configFile(packageName)
        if (!configFile.exists()) return@withContext 0L

        runCatching {
            val json = JSONObject(configFile.readText(Charsets.UTF_8))
            json.optLong(KEY_DELAY_INJECT_MILLIS, 0L).coerceAtLeast(0L)
        }.getOrDefault(0L)
    }

    suspend fun saveDelayInjectMillis(
        envType: String,
        packageName: String,
        delayMillis: Long,
    ) = withContext(Dispatchers.IO) {
        if (!isNoEnvPackage(envType, packageName)) return@withContext
        val configDir = runtimeRootDirectory(packageName)
        ensureDirectory(configDir)

        val configFile = File(configDir, CONFIG_FILE_NAME)
        val json = if (configFile.exists()) {
            runCatching { JSONObject(configFile.readText(Charsets.UTF_8)) }
                .getOrDefault(JSONObject())
        } else {
            JSONObject()
        }
        json.put(KEY_DELAY_INJECT_MILLIS, delayMillis.coerceAtLeast(0L))
        configFile.writeText(json.toString(2), Charsets.UTF_8)
    }

    private fun isNoEnvPackage(envType: String, packageName: String): Boolean {
        return envType == NO_ENV_STORAGE_VALUE && packageName.isNotBlank()
    }

    private fun ensureDirectory(directory: File) {
        if (!directory.exists() && !directory.mkdirs()) {
            throw IOException("Cannot create Frida config directory: ${directory.absolutePath}")
        }
        if (!directory.isDirectory) {
            throw IOException("Frida config path is not a directory: ${directory.absolutePath}")
        }
    }

    private fun runtimeRootDirectory(packageName: String): File {
        return File(
            Environment.getExternalStorageDirectory(),
            "Android/media/$packageName/frida/noenv",
        )
    }

    private fun configFile(packageName: String): File {
        return File(runtimeRootDirectory(packageName), CONFIG_FILE_NAME)
    }

    private companion object {
        const val NO_ENV_STORAGE_VALUE = "no_env"
        const val CONFIG_FILE_NAME = "fridaConfig.json"
        const val KEY_DELAY_INJECT_MILLIS = "delayInjectMillis"
    }
}
