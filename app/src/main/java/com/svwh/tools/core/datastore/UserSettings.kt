package com.svwh.tools.core.datastore

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.FollowSystem,
    val dynamicColor: Boolean = true,
    val showNoEnvironmentTab: Boolean = true,
    val showEnvironmentTab: Boolean = true,
    val fridaLogCompactStyle: Boolean = false,
    val repackSigningMode: RepackSigningMode = RepackSigningMode.BuiltIn,
    val customSigningKeyUri: String = "",
    val customSigningKeyName: String = "",
    val customSigningKeyStoreType: String = "BKS",
    val customSigningKeyAlias: String = "",
    val customSigningStorePassword: String = "",
    val customSigningKeyPassword: String = "",
)
