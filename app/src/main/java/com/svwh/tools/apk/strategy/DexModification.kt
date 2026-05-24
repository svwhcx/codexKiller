package com.svwh.tools.apk.strategy

import com.svwh.tools.apk.strategy.rewriter.SuperClassChangeWriter
import com.svwh.tools.apk.context.ApkProcessorContext
import com.svwh.tools.apk.MethodSignatureUtil
import com.svwh.tools.apk.strategy.rewriter.MethodChangeWriter
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import org.jf.dexlib2.dexbacked.ZipDexContainer
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.DexFile
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.MultiDexContainer
import org.jf.dexlib2.iface.MultiDexContainer.DexEntry
import org.jf.dexlib2.rewriter.DexRewriter
import org.jf.dexlib2.rewriter.Rewriter
import org.jf.dexlib2.rewriter.RewriterModule
import org.jf.dexlib2.rewriter.Rewriters
import org.jf.dexlib2.writer.io.MemoryDataStore
import org.jf.dexlib2.writer.pool.DexPool
import java.io.ByteArrayInputStream
import java.io.File

/**
 * @description
 * @Author chenxin
 * @Date 2025/7/31 23:59
 */
class DexModification : IApkModification {

    private val noEnvHookApplication = "com.svwh.noenvhook.app.KillerBaseApplication"


    override fun modify(apkProcessorContext: ApkProcessorContext): IApkModification {
        // 1. 校验是否需要修改
        if (apkProcessorContext.applicationName == apkProcessorContext.androidApplication) {
            return this
        }
        // 2. 查找对应的application在哪一个dex中，
        val container = ZipDexContainer(File(apkProcessorContext.apkPath), Opcodes.getDefault())
        val targetDexEntry = resolveDex(container, apkProcessorContext) ?: return this
        val dexFile = targetDexEntry.dexFile
        // 3. 通过dex工具修改对应的dex中的Application。
        // 重写KillerApplication中的父类
        val smaliApplicationName = MethodSignatureUtil.convertSmaliSignature(noEnvHookApplication)
        val smaliAppApplication =
            MethodSignatureUtil.convertSmaliSignature(apkProcessorContext.applicationName)
        val methodRewriter = DexRewriter(object : RewriterModule() {

            override fun getMethodRewriter(rewriters: Rewriters): Rewriter<Method> {
                return MethodChangeWriter(smaliApplicationName)
            }
        })


        val classRewriter = DexRewriter(object : RewriterModule() {
            override fun getClassDefRewriter(rewriters: Rewriters): Rewriter<ClassDef> {
                return SuperClassChangeWriter(smaliAppApplication, smaliApplicationName)
            }
        })

        val dexPool = DexPool(dexFile.opcodes)
        dexFile.classes.forEach {
            if (it.type == smaliAppApplication) {
                apkProcessorContext.originSuperClass = it.superclass!!
                val methodRewrite = methodRewriter.classDefRewriter.rewrite(it)
                val classRewrite = classRewriter.classDefRewriter.rewrite(methodRewrite)
                dexPool.internClass(classRewrite)
            } else {
                dexPool.internClass(it)
            }
        }
        val dataStore = MemoryDataStore()
        dexPool.writeTo(dataStore)
        apkProcessorContext.extraDataNodes.add(
            ExtraDataNode(
                targetDexEntry.entryName,
                ByteArrayInputStream(dataStore.data)
            )
        )
        return this
    }


    private fun resolveDex(
        container: MultiDexContainer<out DexBackedDexFile>,
        apkProcessorContext: ApkProcessorContext
    ): DexEntry<out DexFile>? {
        val applicationName =
            MethodSignatureUtil.convertSmaliSignature(apkProcessorContext.applicationName)
        for (dexEntryName in container.dexEntryNames) {
            val dexEntry = container.getEntry(dexEntryName!!)
            for (classDef in dexEntry!!.dexFile.classes) {
                if (classDef.type == applicationName) {
                    return dexEntry // 返回包含目标类的DEX文件
                }
            }
        }
        return null;
    }
}