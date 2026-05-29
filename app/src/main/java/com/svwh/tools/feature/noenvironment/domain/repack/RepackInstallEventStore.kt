package com.svwh.tools.feature.noenvironment.domain.repack

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class RepackInstallEventStore @Inject constructor() {
    private val _installedPackages = MutableStateFlow<Set<String>>(emptySet())
    val installedPackages: StateFlow<Set<String>> = _installedPackages.asStateFlow()

    fun markInstalled(packageName: String) {
        if (packageName.isBlank()) return
        _installedPackages.update { packages -> packages + packageName }
    }

    fun consume(packageName: String) {
        _installedPackages.update { packages -> packages - packageName }
    }
}
