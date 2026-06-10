package com.svwh.tools.apk.strategy

import android.os.Build
import java.util.zip.ZipFile

object InjectedAbiSelector {
    fun selectForCurrentDevice(
        zipFile: ZipFile,
        availableAbis: Collection<String>,
    ): Set<String> {
        val available = availableAbis.toSet()
        if (available.isEmpty()) {
            return emptySet()
        }

        val sourceAbis = zipFile.entries().asSequence()
            .mapNotNull { entry -> LIB_ENTRY_REGEX.matchEntire(entry.name)?.groupValues?.get(1) }
            .filter { abi -> abi in available }
            .toSet()

        if (sourceAbis.isNotEmpty()) {
            Build.SUPPORTED_ABIS.firstOrNull { abi -> abi in sourceAbis }?.let { return setOf(it) }
            return setOf(sourceAbis.first())
        }

        return Build.SUPPORTED_ABIS
            .firstOrNull { abi -> abi in available }
            ?.let { setOf(it) }
            ?: emptySet()
    }

    private val LIB_ENTRY_REGEX = Regex("""lib/([^/]+)/.+\.so""")
}
