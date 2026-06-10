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
            {"interaction":{"type":"script","path":"/storage/emulated/0/Android/media/${apkProcessorContext.packageName}/frida/noenv/killer-frida.js","on_change": "ignore"}}
        """.trimIndent()
        val selectedAbis = InjectedAbiSelector.selectForCurrentDevice(
            zipFile = apkProcessorContext.apkZipFile,
            availableAbis = FRIDA_ABIS.map { it.apkLibDir },
        )
        FRIDA_ABIS.filter { abi -> abi.apkLibDir in selectedAbis }.forEach { abi ->
            addFridaGadget(
                apkProcessorContext = apkProcessorContext,
                abiDir = abi.apkLibDir,
                assetPath = abi.assetPath,
                injectConfig = injectConfig,
            )
        }
        return this
    }

    private fun addFridaGadget(
        apkProcessorContext: ApkProcessorContext,
        abiDir: String,
        assetPath: String,
        injectConfig: String,
    ) {
        apkProcessorContext.extraDataNodes.add(
            ExtraDataNode(
                "lib/$abiDir/libkiller-inject.so",
                apkProcessorContext.context.assets.open(assetPath),
            ),
        )
        apkProcessorContext.extraDataNodes.add(
            ExtraDataNode(
                "lib/$abiDir/libkiller-inject.config.so",
                ByteArrayInputStream(injectConfig.toByteArray()),
            ),
        )
    }

    private data class FridaAbi(
        val apkLibDir: String,
        val assetPath: String,
    )

    private companion object {
        val FRIDA_ABIS = listOf(
            FridaAbi(
                apkLibDir = "armeabi-v7a",
                assetPath = "conf/v7a/killer-inject.so",
            ),
            FridaAbi(
                apkLibDir = "arm64-v8a",
                assetPath = "conf/v8a/killer-inject.so",
            ),
            FridaAbi(
                apkLibDir = "x86",
                assetPath = "conf/x86/killer-inject.so",
            ),
            FridaAbi(
                apkLibDir = "x86_64",
                assetPath = "conf/x86_64/killer-inject.so",
            ),
        )
    }
}
