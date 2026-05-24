package com.svwh.tools.feature.noenvironment.data.repack

import com.svwh.tools.feature.noenvironment.domain.model.AppendRepackStepCommand
import com.svwh.tools.feature.noenvironment.domain.model.RepackProgressState
import com.svwh.tools.feature.noenvironment.domain.model.RepackStep
import com.svwh.tools.feature.noenvironment.domain.model.RepackStepStatus
import com.svwh.tools.feature.noenvironment.domain.model.UpdateRepackStepCommand
import com.svwh.tools.feature.noenvironment.domain.repack.RepackProgressController
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class RepackProgressControllerImpl @Inject constructor() : RepackProgressController {
    private val _state = MutableStateFlow(RepackProgressState())
    override val state: StateFlow<RepackProgressState> = _state.asStateFlow()

    override fun startSession(packageName: String, appName: String) {
        _state.value = RepackProgressState(
            visible = true,
            sessionId = System.currentTimeMillis(),
            packageName = packageName,
            appName = appName,
            steps = emptyList(),
            sessionFinished = false,
        )
    }

    override fun appendStep(command: AppendRepackStepCommand) {
        require(command.stepId.isNotBlank()) { "stepId must not be blank" }
        require(command.label.isNotBlank()) { "label must not be blank" }
        if (command.isTerminal) {
            require(command.terminalOutcome != null) {
                "terminalOutcome is required when isTerminal is true"
            }
        }

        _state.update { current ->
            if (!current.visible) return@update current

            val initialStatus = if (command.autoStart) {
                RepackStepStatus.Running
            } else {
                RepackStepStatus.Pending
            }

            val newStep = RepackStep(
                id = command.stepId,
                label = command.label,
                status = initialStatus,
                isTerminal = command.isTerminal,
                terminalOutcome = command.terminalOutcome,
            )

            current.copy(steps = current.steps + newStep)
        }
    }

    override fun updateStep(command: UpdateRepackStepCommand) {
        _state.update { current ->
            if (!current.visible) return@update current

            val stepIndex = current.steps.indexOfFirst { it.id == command.stepId }
            if (stepIndex < 0) return@update current

            val target = current.steps[stepIndex]
            val updatedStep = target.copy(status = command.status)
            val updatedSteps = current.steps.toMutableList().apply {
                this[stepIndex] = updatedStep
            }

            val sessionFinished = when {
                updatedStep.isTerminal &&
                    (command.status == RepackStepStatus.Success || command.status == RepackStepStatus.Failed) -> true
                else -> current.sessionFinished
            }

            current.copy(
                steps = updatedSteps,
                sessionFinished = sessionFinished,
                outputApkPath = command.outputApkPath ?: current.outputApkPath,
            )
        }
    }

    override fun stopSession() {
        _state.update { current ->
            if (!current.visible || current.sessionFinished) return@update current

            val updatedSteps = current.steps.map { step ->
                if (step.status == RepackStepStatus.Running) {
                    step.copy(status = RepackStepStatus.Failed)
                } else {
                    step
                }
            }

            current.copy(
                steps = updatedSteps,
                sessionFinished = true,
            )
        }
    }

    override fun dismiss() {
        _state.value = RepackProgressState()
    }
}
