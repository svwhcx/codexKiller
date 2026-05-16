package com.svwh.tools.feature.noenvironment.domain.model

/**
 * 由外部执行器下发：追加一个新步骤。
 *
 * @param autoStart 为 true 时，新步骤立即进入 [RepackStepStatus.Running]。
 * @param isTerminal 标记该步骤是否为流程终态节点（成功或失败）。
 * @param terminalOutcome 终态类型，仅当 [isTerminal] 为 true 时需要。
 */
data class AppendRepackStepCommand(
    val stepId: String,
    val label: String,
    val autoStart: Boolean = true,
    val isTerminal: Boolean = false,
    val terminalOutcome: RepackTerminalOutcome? = null,
)
