package com.svwh.tools.feature.noenvironment.data.repack

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.workDataOf
import androidx.work.WorkerParameters
import com.svwh.tools.feature.noenvironment.domain.model.AppendRepackStepCommand
import com.svwh.tools.feature.noenvironment.domain.model.RepackStepStatus
import com.svwh.tools.feature.noenvironment.domain.model.RepackTerminalOutcome
import com.svwh.tools.feature.noenvironment.domain.model.UpdateRepackStepCommand
import com.svwh.tools.feature.noenvironment.domain.repack.RepackProgressController
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

@HiltWorker
class RepackWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val progressController: RepackProgressController,
    private val repackExecutor: RepackApkExecutor,
    private val notificationFactory: RepackWorkerNotificationFactory,
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val packageName = inputData.getString(KEY_PACKAGE_NAME).orEmpty()
        val appName = inputData.getString(KEY_APP_NAME).orEmpty()
        if (packageName.isBlank()) return Result.failure()

        setForeground(notificationFactory.createForegroundInfo(id, appName, "准备重打包", 0, true))
        val currentState = progressController.state.value
        if (!currentState.visible || currentState.packageName != packageName || currentState.sessionFinished) {
            progressController.startSession(
                packageName = packageName,
                appName = appName.ifBlank { packageName },
            )
        }
        ensureQueueStep()
        val sessionId = progressController.state.value.sessionId
        progressController.updateStep(
            UpdateRepackStepCommand(
                stepId = STEP_QUEUE,
                status = RepackStepStatus.Success,
            ),
        )

        val reporter = WorkerRepackReporter(
            appName = appName.ifBlank { packageName },
            sessionId = sessionId,
        )
        return try {
            val outputApk = repackExecutor.execute(
                packageName = packageName,
                outputBaseName = packageName,
                reporter = reporter,
            )
            reporter.finish(
                success = true,
                outputApkPath = outputApk.absolutePath,
                message = "重打包完成",
            )
            Result.success()
        } catch (cancelled: CancellationException) {
            reporter.finish(
                success = false,
                outputApkPath = null,
                message = "已停止",
            )
            Result.failure()
        } catch (throwable: Throwable) {
            reporter.finish(
                success = false,
                outputApkPath = null,
                message = throwable.message ?: "重打包失败",
                detail = throwable.stackTraceToString(),
            )
            Result.failure(
                workDataOf(
                    KEY_ERROR_MESSAGE to (throwable.message ?: "重打包失败"),
                    KEY_ERROR_DETAIL to throwable.stackTraceToString(),
                ),
            )
        }
    }

    private fun ensureQueueStep() {
        val hasQueueStep = progressController.state.value.steps.any { step ->
            step.id == STEP_QUEUE
        }
        if (hasQueueStep) return
        progressController.appendStep(
            AppendRepackStepCommand(
                stepId = STEP_QUEUE,
                label = "启动后台任务",
                autoStart = true,
            ),
        )
    }

    private inner class WorkerRepackReporter(
        private val appName: String,
        private val sessionId: Long,
    ) : RepackExecutionReporter {
        private var progress = 0

        override suspend fun beginStep(stepId: String, label: String, progressPercent: Int) {
            currentCoroutineContext().ensureActive()
            if (isStaleSession()) return
            progress = progressPercent.coerceIn(0, 99)
            progressController.appendStep(
                AppendRepackStepCommand(
                    stepId = stepId,
                    label = label,
                    autoStart = true,
                ),
            )
            setForeground(
                notificationFactory.createForegroundInfo(
                    workId = id,
                    appName = appName,
                    content = label,
                    progress = progress,
                    indeterminate = false,
                ),
            )
        }

        override suspend fun completeStep(stepId: String) {
            currentCoroutineContext().ensureActive()
            if (isStaleSession()) return
            progressController.updateStep(
                UpdateRepackStepCommand(
                    stepId = stepId,
                    status = RepackStepStatus.Success,
                ),
            )
        }

        override suspend fun updateDetail(stepId: String, detail: String) {
            currentCoroutineContext().ensureActive()
            if (isStaleSession()) return
            setForeground(
                notificationFactory.createForegroundInfo(
                    workId = id,
                    appName = appName,
                    content = detail,
                    progress = progress,
                    indeterminate = false,
                ),
            )
        }

        override suspend fun failStep(stepId: String) {
            if (isStaleSession()) return
            progressController.updateStep(
                UpdateRepackStepCommand(
                    stepId = stepId,
                    status = RepackStepStatus.Failed,
                ),
            )
        }

        suspend fun finish(
            success: Boolean,
            outputApkPath: String?,
            message: String,
            detail: String? = null,
        ) {
            if (isStaleSession()) return
            val stepId = if (success) STEP_FINISH_SUCCESS else STEP_FINISH_FAILED
            progressController.appendStep(
                AppendRepackStepCommand(
                    stepId = stepId,
                    label = message,
                    autoStart = true,
                    isTerminal = true,
                    terminalOutcome = if (success) {
                        RepackTerminalOutcome.Success
                    } else {
                        RepackTerminalOutcome.Failure
                    },
                    detail = detail,
                ),
            )
            progressController.updateStep(
                UpdateRepackStepCommand(
                    stepId = stepId,
                    status = if (success) RepackStepStatus.Success else RepackStepStatus.Failed,
                    outputApkPath = outputApkPath,
                ),
            )
            setForeground(
                notificationFactory.createForegroundInfo(
                    workId = id,
                    appName = appName,
                    content = message,
                    progress = if (success) 100 else progress,
                    indeterminate = false,
                    ongoing = false,
                ),
            )
        }

        private fun isStaleSession(): Boolean {
            return progressController.state.value.sessionId != sessionId
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "no_env_repack_work"
        const val WORK_TAG = "no_env_repack"
        const val KEY_PACKAGE_NAME = "package_name"
        const val KEY_APP_NAME = "app_name"
        const val KEY_ERROR_MESSAGE = "error_message"
        const val KEY_ERROR_DETAIL = "error_detail"
        const val STEP_QUEUE = "queue"
        const val STEP_FINISH_SUCCESS = "finish_success"
        const val STEP_FINISH_FAILED = "finish_failed"
    }
}
