package com.svwh.tools.core.hook

enum class HookEnvironmentType(
    val storageValue: String,
    val enableFileName: String,
) {
    WithEnv(
        storageValue = "with_env",
        enableFileName = "with_env_enable.s",
    ),
    NoEnv(
        storageValue = "no_env",
        enableFileName = "no_env_enable.s",
    ),
}
