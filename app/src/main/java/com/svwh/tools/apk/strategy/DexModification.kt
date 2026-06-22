package com.svwh.tools.apk.strategy

import com.svwh.tools.apk.strategy.rewriter.SuperClassChangeWriter
import com.svwh.tools.apk.context.ApkProcessorContext
import com.svwh.tools.apk.MethodSignatureUtil
import com.svwh.tools.apk.strategy.rewriter.MethodChangeWriter
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import org.jf.dexlib2.dexbacked.ZipDexContainer
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.MultiDexContainer
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

        // 3. 找到 Application 继承链
        val applicationClassChain = findApplicationClassChain(container, apkProcessorContext)
        if (applicationClassChain.isEmpty()) {
            return this
        }
        val topLevelAppClass = applicationClassChain.last()

        // 4. 通过dex工具修改对应的dex中的Application
        val smaliApplicationName = MethodSignatureUtil.convertSmaliSignature(noEnvHookApplication)
        val smaliTopLevelClass = MethodSignatureUtil.convertSmaliSignature(topLevelAppClass)
        val smaliApplicationClassChain = applicationClassChain
            .map { MethodSignatureUtil.convertSmaliSignature(it) }
            .toSet()

        val methodRewriter = DexRewriter(object : RewriterModule() {
            override fun getMethodRewriter(rewriters: Rewriters): Rewriter<Method> {
                return MethodChangeWriter(
                    superClazz = smaliApplicationName,
                    clearOnCreate = true,
                    injectAttachBootstrap = true,
                )
            }
        })

        val classRewriter = DexRewriter(object : RewriterModule() {
            override fun getClassDefRewriter(rewriters: Rewriters): Rewriter<ClassDef> {
                return SuperClassChangeWriter(smaliTopLevelClass, smaliApplicationName)
            }
        })

        for (dexEntryName in container.dexEntryNames) {
            val dexEntry = container.getEntry(dexEntryName!!) ?: continue
            val dexFile = dexEntry.dexFile
            var modified = false
            val dexPool = DexPool(dexFile.opcodes)
            dexFile.classes.forEach { classDef ->
                if (classDef.type in smaliApplicationClassChain) {
                    modified = true
                    val methodRewrite = methodRewriter.classDefRewriter.rewrite(classDef)
                    val classRewrite = if (classDef.type == smaliTopLevelClass) {
                        apkProcessorContext.originSuperClass = classDef.superclass!!
                        classRewriter.classDefRewriter.rewrite(methodRewrite)
                    } else {
                        methodRewrite
                    }
                    dexPool.internClass(classRewrite)
                } else {
                    dexPool.internClass(classDef)
                }
            }

            if (modified) {
                val dataStore = MemoryDataStore()
                dexPool.writeTo(dataStore)
                apkProcessorContext.extraDataNodes.add(
                    ExtraDataNode(
                        dexEntry.entryName,
                        ByteArrayInputStream(dataStore.data)
                    )
                )
            }
        }
        return this
    }

    /**
     * 找到 Application 继承链。
     * 例如：MyApp -> BaseApp -> Application，返回 [MyApp, BaseApp]
     * 支持跨多个 DEX 文件的继承链查找
     */
    private fun findApplicationClassChain(
        container: MultiDexContainer<out DexBackedDexFile>,
        apkProcessorContext: ApkProcessorContext
    ): List<String> {
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
        val classChain = mutableListOf<String>()
        var reachedAndroidApplication = false

        // 向上追踪继承链
        while (currentClassName != smaliAndroidApp) {
            val classDef = classMap[currentClassName] ?: break
            val superClass = classDef.superclass ?: break
            classChain += currentClassName

            // 如果父类是 android.app.Application，当前类就是顶层类
            if (superClass == smaliAndroidApp) {
                reachedAndroidApplication = true
                break
            }

            // 继续向上查找
            currentClassName = superClass
        }
        if (!reachedAndroidApplication) {
            return emptyList()
        }

        // 转换回 Java 类名格式：Lcom/example/App; -> com.example.App
        return classChain.map { smaliName ->
            smaliName.removePrefix("L").removeSuffix(";").replace("/", ".")
        }
    }
}
