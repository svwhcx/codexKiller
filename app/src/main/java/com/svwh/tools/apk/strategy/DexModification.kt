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

        // 2. 查找对应的application在哪一个dex中
        val container = ZipDexContainer(File(apkProcessorContext.apkPath), Opcodes.getDefault())

        // 3. 找到继承链的顶层类（直接继承 android.app.Application 的类）
        val topLevelAppClass = findTopLevelApplicationClass(container, apkProcessorContext) ?: return this

        // 4. 找到顶层类所在的 DEX
        val targetDexEntry = resolveDexForClass(container, topLevelAppClass) ?: return this
        val dexFile = targetDexEntry.dexFile

        // 5. 通过dex工具修改对应的dex中的Application
        val smaliApplicationName = MethodSignatureUtil.convertSmaliSignature(noEnvHookApplication)
        val smaliTopLevelClass = MethodSignatureUtil.convertSmaliSignature(topLevelAppClass)

        val methodRewriter = DexRewriter(object : RewriterModule() {
            override fun getMethodRewriter(rewriters: Rewriters): Rewriter<Method> {
                return MethodChangeWriter(smaliApplicationName)
            }
        })

        val classRewriter = DexRewriter(object : RewriterModule() {
            override fun getClassDefRewriter(rewriters: Rewriters): Rewriter<ClassDef> {
                return SuperClassChangeWriter(smaliTopLevelClass, smaliApplicationName)
            }
        })

        val dexPool = DexPool(dexFile.opcodes)
        dexFile.classes.forEach {
            if (it.type == smaliTopLevelClass) {
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


    private fun resolveDexForClass(
        container: MultiDexContainer<out DexBackedDexFile>,
        className: String
    ): DexEntry<out DexFile>? {
        val smaliClassName = MethodSignatureUtil.convertSmaliSignature(className)
        for (dexEntryName in container.dexEntryNames) {
            val dexEntry = container.getEntry(dexEntryName!!)
            for (classDef in dexEntry!!.dexFile.classes) {
                if (classDef.type == smaliClassName) {
                    return dexEntry
                }
            }
        }
        return null
    }

    /**
     * 找到继承链的顶层类（直接继承 android.app.Application 的类）
     * 例如：MyApp -> BaseApp -> Application，返回 BaseApp
     * 支持跨多个 DEX 文件的继承链查找
     */
    private fun findTopLevelApplicationClass(
        container: MultiDexContainer<out DexBackedDexFile>,
        apkProcessorContext: ApkProcessorContext
    ): String? {
        val startClass = apkProcessorContext.applicationName
        val androidAppClass = apkProcessorContext.androidApplication
        val smaliAndroidApp = MethodSignatureUtil.convertSmaliSignature(androidAppClass)

        // 构建所有 DEX 中类名到 ClassDef 的映射
        val classMap = mutableMapOf<String, ClassDef>()
        for (dexEntryName in container.dexEntryNames) {
            val dexEntry = container.getEntry(dexEntryName!!)
            dexEntry!!.dexFile.classes.forEach { classDef ->
                classMap[classDef.type] = classDef
            }
        }

        var currentClassName = MethodSignatureUtil.convertSmaliSignature(startClass)
        var topLevelClass: String? = null

        // 向上追踪继承链
        while (currentClassName != smaliAndroidApp) {
            val classDef = classMap[currentClassName] ?: break
            val superClass = classDef.superclass ?: break

            // 如果父类是 android.app.Application，当前类就是顶层类
            if (superClass == smaliAndroidApp) {
                topLevelClass = currentClassName
                break
            }

            // 继续向上查找
            currentClassName = superClass
        }

        // 转换回 Java 类名格式：Lcom/example/App; -> com.example.App
        return topLevelClass?.let { smaliName ->
            smaliName.removePrefix("L").removeSuffix(";").replace("/", ".")
        }
    }
}