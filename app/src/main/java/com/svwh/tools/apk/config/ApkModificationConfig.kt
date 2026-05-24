package com.svwh.tools.apk.config

/**
 * @description
 * @Author chenxin
 * @Date 2025/8/1 0:04
 */
data class ApkModificationConfig(
    val manifestModificationConfig: List<ManifestModificationConfig>,
    val dexModificationConfig: List<DexModificationConfig>,
    val signConfig: SignConfig,
    val apkSavePath: String
)
