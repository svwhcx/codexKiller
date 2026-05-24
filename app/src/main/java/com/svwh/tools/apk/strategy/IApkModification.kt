package com.svwh.tools.apk.strategy

import com.svwh.tools.apk.context.ApkProcessorContext

/**
 * @description
 * @Author chenxin
 * @Date 2025/7/31 23:58
 */
interface IApkModification {

    fun modify(apkProcessorContext: ApkProcessorContext) : IApkModification
}