package com.svwh.tools.feature.noenvironment.data.repack

import com.svwh.tools.core.common.AppDispatchers
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem
import com.svwh.tools.feature.noenvironment.domain.model.AppendRepackStepCommand
import com.svwh.tools.feature.noenvironment.domain.model.RepackStepStatus
import com.svwh.tools.feature.noenvironment.domain.model.RepackTerminalOutcome
import com.svwh.tools.feature.noenvironment.domain.model.UpdateRepackStepCommand
import com.svwh.tools.feature.noenvironment.domain.repack.RepackProgressController
import com.svwh.tools.feature.noenvironment.domain.repack.RepackWorkflowRunner
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Singleton
class RepackProgressSimulator @Inject constructor(
    private val progressController: RepackProgressController,
    private val dispatchers: AppDispatchers,
) : RepackWorkflowRunner {

    override suspend fun start(app: InstalledAppItem) = withContext(dispatchers.default) {
        progressController.startSession(
            packageName = app.packageName,
            appName = app.appName,
        )

        runStep("read_apk", "读取 APK", durationMs = 900)
        runStep("decode", "反编译资源", durationMs = 1_200)
        runStep("manifest", "校验 Manifest", durationMs = 900)
        runStep("patch", "注入 Hook 逻辑", durationMs = 1_500)
        runStep("optimize", "优化资源文件", durationMs = 1_100)
        runStep("rebuild", "重新打包", durationMs = 1_300)
        runStep("sign", "签名对齐", durationMs = 1_000)
        runStep("verify", "校验安装包", durationMs = 800)

        progressController.appendStep(
            AppendRepackStepCommand(
                stepId = "finish_success",
                label = "打包完成",
                autoStart = true,
                isTerminal = true,
                terminalOutcome = RepackTerminalOutcome.Success,
            ),
        )
        delay(700)
        progressController.updateStep(
            UpdateRepackStepCommand(
                stepId = "finish_success",
                status = RepackStepStatus.Success,
            ),
        )
    }

    override fun stop() = Unit

    private suspend fun runStep(stepId: String, label: String, durationMs: Long) {
        progressController.appendStep(
            AppendRepackStepCommand(
                stepId = stepId,
                label = label,
                autoStart = true,
            ),
        )
        delay(durationMs)
        progressController.updateStep(
            UpdateRepackStepCommand(
                stepId = stepId,
                status = RepackStepStatus.Success,
            ),
        )
    }
}
