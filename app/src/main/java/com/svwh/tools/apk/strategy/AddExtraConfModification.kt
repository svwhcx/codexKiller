package com.svwh.tools.apk.strategy

import com.svwh.tools.apk.strategy.rewriter.SuperClassChangeWriter
import com.svwh.tools.apk.context.ApkProcessorContext
import com.svwh.tools.apk.strategy.rewriter.MethodChangeWriter
import com.svwh.tools.apk.MethodSignatureUtil
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.rewriter.DexRewriter
import org.jf.dexlib2.rewriter.Rewriter
import org.jf.dexlib2.rewriter.RewriterModule
import org.jf.dexlib2.rewriter.Rewriters
import org.jf.dexlib2.writer.io.MemoryDataStore
import org.jf.dexlib2.writer.pool.DexPool
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.util.zip.ZipFile

/**
 * @description
 * @Author chenxin
 * @Date 2025/8/2 17:15
 */
class AddExtraConfModification : IApkModification {
    override fun modify(apkProcessorContext: ApkProcessorContext): IApkModification {
        // 计算dex的数量
        val dexNum = calDexNum(apkProcessorContext.apkZipFile)
        // 加载dex文件为输入流
        var killerDexIns = apkProcessorContext.context.assets?.open("conf/killer_hook.dex")

        // 要修改killer_hook的super_class为指定的


        if (apkProcessorContext.originSuperClass != "android.app.Application") {
            val dexFile = DexBackedDexFile.fromInputStream(
                null,
                BufferedInputStream(killerDexIns)
            )
            val smaliAppApplication =
                MethodSignatureUtil.convertSmaliSignature("com.svwh.noenvhook.app.KillerBaseApplication")
            val smaliAppApplicationName =apkProcessorContext.originSuperClass
            val methodRewriter = DexRewriter(object : RewriterModule() {
                override fun getMethodRewriter(rewriters: Rewriters): Rewriter<Method> {
                    return MethodChangeWriter(smaliAppApplicationName)
                }
            })

            val classRewriter = DexRewriter(object : RewriterModule() {
                override fun getClassDefRewriter(rewriters: Rewriters): Rewriter<ClassDef> {
                    return SuperClassChangeWriter(smaliAppApplication, smaliAppApplicationName)
                }
            })
            val dexPool = DexPool(dexFile.opcodes)
            dexFile.classes.forEach {
                if (it.type == smaliAppApplication) {
                    val methodRewrite = methodRewriter.classDefRewriter.rewrite(it)
                    val classRewrite = classRewriter.classDefRewriter.rewrite(methodRewrite)
                    dexPool.internClass(classRewrite)
                } else {
                    dexPool.internClass(it)
                }
            }
            val dataStore = MemoryDataStore()
            dexPool.writeTo(dataStore)
            killerDexIns = ByteArrayInputStream(dataStore.data)
        }
        apkProcessorContext.extraDataNodes.add(
            ExtraDataNode(
                "classes${dexNum}.dex",
                killerDexIns!!
            )
        )
        // 2. 添加额外的libpine.so文件
        val v7aSo = apkProcessorContext.context.assets?.open("conf/v7a/libpine.so")
        val v8aSo = apkProcessorContext.context.assets?.open("conf/v8a/libpine.so")
        apkProcessorContext.extraDataNodes.add(ExtraDataNode("lib/armeabi-v7a/libpine.so", v7aSo!!))
        apkProcessorContext.extraDataNodes.add(ExtraDataNode("lib/arm64-v8a/libpine.so", v8aSo!!))
        return this
    }

    /**
     * 计算dex的数量
     */
    private fun calDexNum(zipFile: ZipFile): Int {
        var dexNum = 2
        while (true) {
            val path = "classes${dexNum}.dex"
            zipFile.getEntry(path) ?: return dexNum
            dexNum++
        }
    }


}