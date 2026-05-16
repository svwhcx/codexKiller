package com.svwh.tools.feature.noenvironment.domain.repack

import com.svwh.tools.feature.environment.domain.model.InstalledAppItem

/**
 * 重打包流程执行入口。真实实现替换模拟器即可，无需改动 UI。
 */
interface RepackWorkflowRunner {
    suspend fun start(app: InstalledAppItem)
}
