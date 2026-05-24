package com.svwh.tools.apk.pipline

import com.svwh.tools.apk.context.ApkProcessorContext
import com.svwh.tools.axml.AXMLDecoder
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipFile
import javax.xml.parsers.DocumentBuilderFactory

/**
 * @description
 * @Author chenxin
 * @Date 2025/7/31 23:55
 */
class ResolveApkStage() : AbstractApkProcessor() {


    init {
        setNextProcessor(ModifyStage())
    }

    private lateinit var apkProcessorContext: ApkProcessorContext

    override fun doProcess(apkProcessorContext: ApkProcessorContext): ApkProcessorContext {
        setCurrentProcessor(this)
        val apkPath = apkProcessorContext.apkPath
        if (!File(apkPath).exists()) {
            throw Exception("App 已卸载!")
        }
        val zipFile = ZipFile(apkPath)
        this.apkProcessorContext = apkProcessorContext
        apkProcessorContext.apkZipFile = zipFile

        val manifestEntry = apkProcessorContext.apkZipFile.getEntry("AndroidManifest.xml")
        val axmlString = AXMLDecoder()
            .decode(zipFile.getInputStream(manifestEntry))
        // 解析axml文件获取基本数据
        parse(axmlString,apkProcessorContext)
        return apkProcessorContext;
    }

    /**
     * 执行停止的操作
     */
    override fun doStop() {
        // 解析阶段直接停止。
        // 1. 停止zipFile
        apkProcessorContext.apkZipFile.close()
    }


    /**
     * 解析xml文本，获取里面的application、packageName、versionName等等。
     */
    private fun parse(xml: String,apkProcessorContext: ApkProcessorContext){

        val builderFactory = DocumentBuilderFactory.newInstance();

        val builder = builderFactory.newDocumentBuilder();
        val document = builder.parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)));
        apkProcessorContext.axmlDocument  = document
        val manifestNode = document.getElementsByTagName("manifest").item(0);
        // 记录包名
        apkProcessorContext.packageName = manifestNode.attributes.getNamedItem("package").nodeValue
        val nodes = manifestNode.childNodes;
        for (i in 0..nodes.length) {
            val node = nodes.item(i)
            // 寻找Application入口类，修改并保存入口类
            if (node.nodeName.equals("application")) {
                // 获取android:name属性
                val namedItem = node.attributes.getNamedItem("android:name")
                if (namedItem != null) {
                    val clazz = namedItem.nodeValue
                    apkProcessorContext.applicationName = if (clazz.startsWith(".")){
                        apkProcessorContext.packageName + clazz
                    }else{
                        clazz
                    }
                }
                break;
            }
        }
    }
}