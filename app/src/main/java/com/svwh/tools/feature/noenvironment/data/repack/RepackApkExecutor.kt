package com.svwh.tools.feature.noenvironment.data.repack

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import com.svwh.tools.apk.config.ApkModificationConfig
import com.svwh.tools.apk.config.SignConfig
import com.svwh.tools.apk.context.ApkProcessorContext
import com.svwh.tools.apk.observer.IProcessEventObserver
import com.svwh.tools.apk.observer.ProcessEvent
import com.svwh.tools.apk.observer.ProcessEventResult
import com.svwh.tools.apk.observer.ProcessEventType
import com.svwh.tools.apk.pipline.ResolveApkStage
import com.svwh.tools.core.common.AppDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

@Singleton
class RepackApkExecutor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppDispatchers,
) {
    suspend fun execute(
        packageName: String,
        outputBaseName: String,
        reporter: RepackExecutionReporter,
    ): File = withContext(dispatchers.io) {
        val safeName = outputBaseName.toSafeFileName()
        val sessionRoot = File(context.cacheDir, "no_env_repack/$safeName").apply {
            deleteRecursively()
            mkdirs()
        }
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(context.filesDir, "repack-output")
        outputDir.mkdirs()

        val unsignedApk = File(sessionRoot, "$safeName.apk")
        val signedApk = File(sessionRoot, "$safeName" + "_sign.apk")
        val outputApk = File(outputDir, "$safeName-killer.apk")

        try {
            val sourceApk = runStepWithResult(reporter, "read_apk", "读取 APK", 8) {
                val appInfo = getApplicationInfo(packageName)
                val splitSources = appInfo.splitSourceDirs.orEmpty()
                require(splitSources.isEmpty()) {
                    "该应用是 Split APK，当前无环境写入流程暂不支持"
                }
                File(appInfo.sourceDir).also { apk ->
                    require(apk.exists()) { "APK 文件不存在，应用可能已卸载" }
                }
            }

            runStep(reporter, "check_assets", "检查内置资源", 16) {
                REQUIRED_ASSETS.forEach { assetPath ->
                    require(assetExists(assetPath)) {
                        "缺少 assets/$assetPath"
                    }
                }
            }

            runStep(reporter, "rewrite_apk", "写入无环境 Hook", 48) {
                currentCoroutineContext().ensureActive()
                val processorContext = ApkProcessorContext().apply {
                    this.context = this@RepackApkExecutor.context
                    apkPath = sourceApk.absolutePath
                    apkModificationConfig = ApkModificationConfig(
                        manifestModificationConfig = emptyList(),
                        dexModificationConfig = emptyList(),
                        signConfig = SignConfig(),
                        apkSavePath = unsignedApk.absolutePath,
                    )
                    processEventObservers.add(
                        object : IProcessEventObserver {
                            override fun onProcessEvent(event: ProcessEvent) {
                                if (event.eventType == ProcessEventType.END &&
                                    event.result == ProcessEventResult.FAIL
                                ) {
                                    throw IllegalStateException(event.message)
                                }
                            }
                        },
                    )
                }
                ResolveApkStage().process(processorContext)
            }

            runStep(reporter, "copy_output", "保存安装包", 92) {
                require(signedApk.exists() && signedApk.length() > 0L) {
                    "签名后的 APK 不存在或为空"
                }
                signedApk.copyTo(outputApk, overwrite = true)
            }

            runStep(reporter, "verify", "校验安装包", 98) {
                require(outputApk.exists() && outputApk.length() > 0L) {
                    "输出 APK 不存在或为空"
                }
            }

            outputApk
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (throwable: Throwable) {
            throw IllegalStateException(throwable.message ?: "重打包失败", throwable)
        }
    }

    private suspend fun runStep(
        reporter: RepackExecutionReporter,
        stepId: String,
        label: String,
        progressPercent: Int,
        block: suspend () -> Unit,
    ) {
        reporter.beginStep(stepId, label, progressPercent)
        try {
            currentCoroutineContext().ensureActive()
            block()
            reporter.completeStep(stepId)
        } catch (throwable: Throwable) {
            reporter.failStep(stepId)
            throw throwable
        }
    }

    private suspend fun <T> runStepWithResult(
        reporter: RepackExecutionReporter,
        stepId: String,
        label: String,
        progressPercent: Int,
        block: suspend () -> T,
    ): T {
        reporter.beginStep(stepId, label, progressPercent)
        return try {
            currentCoroutineContext().ensureActive()
            val result = block()
            reporter.completeStep(stepId)
            result
        } catch (throwable: Throwable) {
            reporter.failStep(stepId)
            throw throwable
        }
    }

    private fun getApplicationInfo(packageName: String): ApplicationInfo {
        val packageManager = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(0),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(packageName, 0)
        }
    }

    private fun assetExists(path: String): Boolean {
        return runCatching {
            context.assets.open(path).use { true }
        }.getOrDefault(false)
    }

    private fun String.toSafeFileName(): String {
        return replace(Regex("""[^A-Za-z0-9._-]"""), "_").ifBlank { "repacked" }
    }

    private companion object {
        val REQUIRED_ASSETS = listOf(
            "conf/killer_hook.dex",
            "conf/killer.bks",
            "conf/v7a/libpine.so",
            "conf/v7a/killer-inject.so",
            "conf/v8a/libpine.so",
            "conf/v8a/killer-inject.so",
            "conf/x86/killer-inject.so",
            "conf/x86_64/killer-inject.so",
        )
    }
}
