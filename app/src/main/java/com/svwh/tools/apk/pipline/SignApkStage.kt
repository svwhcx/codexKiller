package com.svwh.tools.apk.pipline

import android.net.Uri
import com.svwh.tools.apk.SignUtils
import com.svwh.tools.apk.config.SignConfig
import com.svwh.tools.apk.context.ApkProcessorContext
import com.svwh.tools.apk.observer.ProcessEvent
import com.svwh.tools.apk.observer.ProcessEventResult
import com.svwh.tools.apk.observer.ProcessEventType
import java.io.File

/**
 * @description
 * @Author chenxin
 * @Date 2025/7/31 23:56
 */
class SignApkStage : AbstractApkProcessor() {

    override fun doProcess(apkProcessorContext: ApkProcessorContext): ApkProcessorContext {
        setCurrentProcessor(this)
        val signConfig = apkProcessorContext.apkModificationConfig.signConfig
        val signApkPath = apkProcessorContext.apkModificationConfig.apkSavePath.replace(".apk", "_sign.apk")
        val unsignedApk = File(apkProcessorContext.apkModificationConfig.apkSavePath)
        val signedApk = File(signApkPath)

        signOnce(apkProcessorContext, signConfig, unsignedApk, signedApk)

        unsignedApk.delete()
        val completedEvent = ProcessEvent(ProcessEventType.END, "签名完成", "", ProcessEventResult.SUCCESS)
        apkProcessorContext.dispatchEvent(completedEvent)
        return apkProcessorContext
    }

    /**
     * 执行停止写出操作
     */
    override fun doStop() {
    }

    private fun signOnce(
        apkProcessorContext: ApkProcessorContext,
        signConfig: SignConfig,
        inputApk: File,
        outputApk: File,
    ) {
        openKeyStore(apkProcessorContext, signConfig).use { keyStore ->
            SignUtils.signApk(
                key = keyStore,
                keyStoreType = signConfig.keyStoreType,
                storePassword = signConfig.storePassword,
                keyPassword = signConfig.keyPassword,
                alias = signConfig.alias,
                inputApk = inputApk,
                output = outputApk,
            )
        }
    }

    private fun openKeyStore(
        apkProcessorContext: ApkProcessorContext,
        signConfig: SignConfig,
    ) = when (signConfig.source) {
        SignConfig.Source.BuiltIn -> {
            apkProcessorContext.context.assets.open(SignConfig.BUILT_IN_KEY_ASSET_PATH)
        }
        SignConfig.Source.Custom -> {
            require(signConfig.keyUri.isNotBlank()) {
                "未选择自定义签名密钥文件"
            }
            apkProcessorContext.context.contentResolver.openInputStream(Uri.parse(signConfig.keyUri))
                ?: error("无法打开自定义签名密钥文件")
        }
    }

}
