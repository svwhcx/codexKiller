package com.svwh.tools.feature.noenvironment.domain.repack

import android.graphics.drawable.Drawable
import com.svwh.tools.feature.noenvironment.domain.model.AppendRepackStepCommand
import com.svwh.tools.feature.noenvironment.domain.model.RepackProgressState
import com.svwh.tools.feature.noenvironment.domain.model.UpdateRepackStepCommand
import kotlinx.coroutines.flow.StateFlow

/**
 * 重打包进度状态机，供 UI 订阅、供外部执行器驱动。
 * UI 只读 [state]；所有变更通过本接口显式下发。
 */
interface RepackProgressController {
    val state: StateFlow<RepackProgressState>

    fun startSession(packageName: String, appName: String, appIcon: Drawable? = null)

    fun appendStep(command: AppendRepackStepCommand)

    fun updateStep(command: UpdateRepackStepCommand)

    fun stopSession()

    fun dismiss()
}
