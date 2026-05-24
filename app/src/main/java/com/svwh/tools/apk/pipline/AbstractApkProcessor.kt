package com.svwh.tools.apk.pipline

import com.svwh.tools.apk.context.ApkProcessorContext

/**
 * @description
 * @Author chenxin
 * @Date 2025/7/31 23:50
 */
abstract class AbstractApkProcessor(): IApkProcessor {

    protected var nextProcessor: IApkProcessor? = null
    private var currentProcessor: IApkProcessor? = null

    fun setNextProcessor(nextProcessor: IApkProcessor): IApkProcessor {
        this.nextProcessor = nextProcessor
        return nextProcessor
    }

    override fun process(apkProcessorContext: ApkProcessorContext): ApkProcessorContext {
        val context = doProcess(apkProcessorContext)
        currentProcessor = nextProcessor
        return nextProcessor?.process(apkProcessorContext) ?: context
    }

    override fun stop() {
        // 调用执行执行的
        currentProcessor?.doStop()
    }

    protected fun setCurrentProcessor(currentProcessor: IApkProcessor) {
        this.currentProcessor = currentProcessor
    }

    protected abstract fun doProcess(apkProcessorContext: ApkProcessorContext): ApkProcessorContext


}