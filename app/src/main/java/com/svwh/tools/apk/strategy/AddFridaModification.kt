package com.svwh.tools.apk.strategy

import com.svwh.tools.apk.context.ApkProcessorContext
import java.io.ByteArrayInputStream

/**
 * 添加frida到apk中
 * @description
 * @Author chenxin
 * @Date 2025/8/3 10:39
 */
class AddFridaModification: IApkModification {
    override fun modify(apkProcessorContext: ApkProcessorContext): IApkModification {
        // 1. 添加frida
        // 2. 对fridaConfig进行重写操作
        val injectConfig = """
            {"interaction":{"type":"script-directory","path":"/storage/emulated/0/Android/media/${apkProcessorContext.packageName}/frida/noenv"}}
        """.trimIndent()
        val fridaNode = ExtraDataNode("lib/arm64-v8a/libkiller-inject.so",apkProcessorContext.context.assets.open("conf/v8a/killer-inject.so"))
        val fridaConfigNode = ExtraDataNode("lib/arm64-v8a/libkiller-inject.config.so",ByteArrayInputStream(injectConfig.toByteArray()))

        apkProcessorContext.extraDataNodes.add(fridaNode)
        apkProcessorContext.extraDataNodes.add(fridaConfigNode)
        return this
    }
}
