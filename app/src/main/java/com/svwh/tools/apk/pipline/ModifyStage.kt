package com.svwh.tools.apk.pipline

import com.svwh.tools.apk.context.ApkProcessorContext
import com.svwh.tools.apk.strategy.AddExtraConfModification
import com.svwh.tools.apk.strategy.AddFridaModification
import com.svwh.tools.apk.strategy.DexModification
import com.svwh.tools.apk.strategy.ManifestModify

/**
 * 修改apk操作
 * @description
 * @Author chenxin
 * @Date 2025/7/31 23:56
 */
class ModifyStage: AbstractApkProcessor() {

    init {
        setNextProcessor(RepackStage())
    }

    override fun doProcess(apkProcessorContext: ApkProcessorContext): ApkProcessorContext {
        setCurrentProcessor(this)

        val dexModification = DexModification()
        val manifestModify = ManifestModify()
        val addExtraConfModification = AddExtraConfModification()
        val addFridaModification = AddFridaModification()
        dexModification.modify(apkProcessorContext)
        addExtraConfModification.modify(apkProcessorContext)
        manifestModify.modify(apkProcessorContext)
        addFridaModification.modify(apkProcessorContext)
        return apkProcessorContext
    }

    /**
     * 执行停止的操作
     */
    override fun doStop() {
        TODO("Not yet implemented")
    }
}