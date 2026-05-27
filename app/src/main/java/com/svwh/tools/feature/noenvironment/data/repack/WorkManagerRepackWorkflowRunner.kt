package com.svwh.tools.feature.noenvironment.data.repack

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import androidx.lifecycle.Observer
import com.svwh.tools.feature.environment.domain.model.InstalledAppItem
import com.svwh.tools.feature.noenvironment.domain.model.AppendRepackStepCommand
import com.svwh.tools.feature.noenvironment.domain.model.RepackStepStatus
import com.svwh.tools.feature.noenvironment.domain.model.RepackTerminalOutcome
import com.svwh.tools.feature.noenvironment.domain.model.UpdateRepackStepCommand
import com.svwh.tools.feature.noenvironment.domain.repack.RepackProgressController
import com.svwh.tools.feature.noenvironment.domain.repack.RepackWorkflowRunner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerRepackWorkflowRunner @Inject constructor(
    @ApplicationContext context: Context,
    private val progressController: RepackProgressController,
) : RepackWorkflowRunner {
    private val workManager = WorkManager.getInstance(context)
    private val directExecutor = Executor { runnable -> runnable.run() }

    override suspend fun start(app: InstalledAppItem) {
        progressController.startSession(
            packageName = app.packageName,
            appName = app.appName,
            appIcon = app.icon,
        )
        progressController.appendStep(
            AppendRepackStepCommand(
                stepId = RepackWorker.STEP_QUEUE,
                label = "启动后台任务",
                autoStart = true,
            ),
        )

        val request = OneTimeWorkRequestBuilder<RepackWorker>()
            .setInputData(
                workDataOf(
                    RepackWorker.KEY_PACKAGE_NAME to app.packageName,
                    RepackWorker.KEY_APP_NAME to app.appName,
                ),
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                10,
                TimeUnit.SECONDS,
            )
            .addTag(RepackWorker.WORK_TAG)
            .addTag(app.packageName)
            .build()

        observeWorkState(
            requestId = request.id,
            packageName = app.packageName,
        )

        val operation = try {
            workManager.enqueueUniqueWork(
                RepackWorker.UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        } catch (throwable: Throwable) {
            appendTerminalFailure(
                message = throwable.message ?: "后台任务提交失败",
                detail = throwable.stackTraceToString(),
            )
            return
        }

        operation.result.addListener(
            {
                try {
                    operation.result.get()
                } catch (throwable: Throwable) {
                    appendTerminalFailure(
                        message = throwable.cause?.message ?: throwable.message ?: "后台任务提交失败",
                        detail = throwable.cause?.stackTraceToString() ?: throwable.stackTraceToString(),
                    )
                }
            },
            directExecutor,
        )
    }

    override fun stop() {
        workManager.cancelUniqueWork(RepackWorker.UNIQUE_WORK_NAME)
    }

    private fun observeWorkState(
        requestId: UUID,
        packageName: String,
    ) {
        val workInfoLiveData = workManager.getWorkInfoByIdLiveData(requestId)
        val observer = object : Observer<WorkInfo?> {
            override fun onChanged(workInfo: WorkInfo?) {
                if (workInfo == null) return
                val state = progressController.state.value
                if (!state.visible || state.packageName != packageName || state.sessionFinished) {
                    if (workInfo.state.isFinished) {
                        workInfoLiveData.removeObserver(this)
                    }
                    return
                }
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        progressController.updateStep(
                            UpdateRepackStepCommand(
                                stepId = RepackWorker.STEP_QUEUE,
                                status = RepackStepStatus.Success,
                            ),
                        )
                    }
                    WorkInfo.State.FAILED -> {
                        appendTerminalFailure(
                            message = workInfo.outputData.getString(RepackWorker.KEY_ERROR_MESSAGE)
                                ?: "后台任务启动失败",
                            detail = workInfo.outputData.getString(RepackWorker.KEY_ERROR_DETAIL),
                        )
                        workInfoLiveData.removeObserver(this)
                    }
                    WorkInfo.State.CANCELLED -> {
                        progressController.stopSession()
                        workInfoLiveData.removeObserver(this)
                    }
                    else -> Unit
                }
                if (workInfo.state.isFinished) {
                    workInfoLiveData.removeObserver(this)
                }
            }
        }
        workInfoLiveData.observeForever(observer)
    }

    private fun appendTerminalFailure(
        message: String,
        detail: String? = null,
    ) {
        progressController.updateStep(
            UpdateRepackStepCommand(
                stepId = RepackWorker.STEP_QUEUE,
                status = RepackStepStatus.Failed,
            ),
        )
        progressController.appendStep(
            AppendRepackStepCommand(
                stepId = RepackWorker.STEP_FINISH_FAILED,
                label = message,
                autoStart = true,
                isTerminal = true,
                terminalOutcome = RepackTerminalOutcome.Failure,
                detail = detail,
            ),
        )
        progressController.updateStep(
            UpdateRepackStepCommand(
                stepId = RepackWorker.STEP_FINISH_FAILED,
                status = RepackStepStatus.Failed,
            ),
        )
    }
}
