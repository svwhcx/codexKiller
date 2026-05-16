package com.svwh.tools.feature.noenvironment.domain.model

/**
 * 由外部执行器下发：更新已有步骤状态。
 */
data class UpdateRepackStepCommand(
    val stepId: String,
    val status: RepackStepStatus,
)
