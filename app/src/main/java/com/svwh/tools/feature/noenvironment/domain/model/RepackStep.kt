package com.svwh.tools.feature.noenvironment.domain.model

data class RepackStep(
    val id: String,
    val label: String,
    val status: RepackStepStatus,
    val isTerminal: Boolean = false,
    val terminalOutcome: RepackTerminalOutcome? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
)
