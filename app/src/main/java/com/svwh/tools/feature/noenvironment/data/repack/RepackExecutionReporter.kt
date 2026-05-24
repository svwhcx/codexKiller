package com.svwh.tools.feature.noenvironment.data.repack

interface RepackExecutionReporter {
    suspend fun beginStep(stepId: String, label: String, progressPercent: Int)

    suspend fun completeStep(stepId: String)

    suspend fun updateDetail(stepId: String, detail: String)

    suspend fun failStep(stepId: String)
}
