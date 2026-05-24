package com.svwh.tools.apk.pipline

import com.svwh.tools.apk.context.ApkProcessorContext

/**
 * @description
 * @Author chenxin
 * @Date 2025/7/31 23:49
 */
interface IApkProcessor {

    fun process(apkProcessorContext: ApkProcessorContext): ApkProcessorContext

    /**
     * 停止处理要执行的操作
     */
    fun stop()

    fun doStop()
}