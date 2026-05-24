package com.svwh.tools.apk.pipline

import com.svwh.tools.apk.SignUtils
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
class SignApkStage: AbstractApkProcessor() {


    override fun doProcess(apkProcessorContext: ApkProcessorContext): ApkProcessorContext {
        setCurrentProcessor(this)
        val keyStore = apkProcessorContext.context.assets.open("conf/killer.bks")
        val signApkPath = apkProcessorContext.apkModificationConfig.apkSavePath.replace(".apk", "_sign.apk")
        val newApkFile = File(apkProcessorContext.apkModificationConfig.apkSavePath)
        SignUtils.signApk(keyStore, "svwh.killer", newApkFile, File(signApkPath))
        newApkFile.delete()
        val completedEvent = ProcessEvent(ProcessEventType.END, "签名完成", "", ProcessEventResult.SUCCESS)
        apkProcessorContext.dispatchEvent(completedEvent)
        return apkProcessorContext
    }

    /**
     * 执行停止写出的操作
     */
    override fun doStop() {

    }
}