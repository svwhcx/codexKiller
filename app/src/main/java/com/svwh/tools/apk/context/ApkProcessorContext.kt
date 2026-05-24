package com.svwh.tools.apk.context

import android.content.Context
import com.svwh.tools.apk.config.ApkModificationConfig
import com.svwh.tools.apk.observer.IProcessEventObserver
import com.svwh.tools.apk.observer.ProcessEvent
import com.svwh.tools.apk.strategy.ExtraDataNode
import org.w3c.dom.Document
import java.util.zip.ZipFile

/**
 * @description
 * @Author chenxin
 * @Date 2025/7/31 23:50
 */
class ApkProcessorContext {

    /**
     * apk对应的解析的zip文件
     */
    lateinit var apkZipFile: ZipFile

    lateinit var context: Context

    var originSuperClass: String =  "android.app.Application";

    /**
     * 处理进度的观察者
     */
    val processEventObservers: MutableList<IProcessEventObserver> = mutableListOf()

    /**
     * apk的安装路径
     */
    lateinit var apkPath: String

    /**
     * 额外的数据节点，包括对应的文件流
     */
    val extraDataNodes: MutableList<ExtraDataNode> = mutableListOf()

    var axmlDocument: Document? = null

    var packageName: String = "";

    lateinit var apkModificationConfig: ApkModificationConfig

    val androidApplication = "android.app.Application"

    /**
     * apk的ApplicationName，默认为Android的Application类。
     */
    var applicationName = androidApplication


    fun dispatchEvent(processEvent: ProcessEvent){
        processEventObservers.forEach {
            it.onProcessEvent(processEvent)
        }
    }
}