package com.svwh.tools.core.datastore

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.FollowSystem,
    val dynamicColor: Boolean = true,
)
