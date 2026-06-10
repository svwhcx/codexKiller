package com.svwh.tools.apk.config

/**
 * @description
 * @Author chenxin
 * @Date 2025/8/1 0:08
 */
data class SignConfig(
    val source: Source = Source.BuiltIn,
    val keyUri: String = "",
    val keyStoreType: String = DEFAULT_KEY_STORE_TYPE,
    val storePassword: String = DEFAULT_STORE_PASSWORD,
    val keyPassword: String = DEFAULT_KEY_PASSWORD,
    val alias: String = "",
) {
    enum class Source {
        BuiltIn,
        Custom,
    }

    companion object {
        const val BUILT_IN_KEY_ASSET_PATH = "sign/killer.bks"
        const val DEFAULT_KEY_STORE_TYPE = "BKS"
        const val DEFAULT_STORE_PASSWORD = "svwh.killer"
        const val DEFAULT_KEY_PASSWORD = "killer.svwh"
    }
}
