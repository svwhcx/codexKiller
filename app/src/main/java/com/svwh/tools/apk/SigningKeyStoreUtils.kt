package com.svwh.tools.apk

import java.security.KeyStore
import java.util.Locale

object SigningKeyStoreUtils {
    val supportedKeyStoreTypes = listOf("BKS", "JKS", "PKCS12")

    private val supportedExtensions = setOf("bks", "jks", "p12", "pkcs12")

    data class LoadedKeyStore(
        val type: String,
        val keyStore: KeyStore,
    )

    fun normalizeKeyStoreType(value: String): String {
        return when (value.trim().uppercase(Locale.ROOT).replace("-", "").replace("_", "")) {
            "JKS" -> "JKS"
            "PKCS12", "PKCS#12", "P12" -> "PKCS12"
            else -> "BKS"
        }
    }

    fun keyStoreTypeFromFileName(fileName: String): String? {
        return when (fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)) {
            "bks" -> "BKS"
            "jks" -> "JKS"
            "p12", "pkcs12" -> "PKCS12"
            else -> null
        }
    }

    fun isSupportedSigningKeyName(fileName: String): Boolean {
        return fileName.substringAfterLast('.', "").lowercase(Locale.ROOT) in supportedExtensions
    }

    fun loadKeyStore(
        keyStoreBytes: ByteArray,
        preferredType: String,
        storePassword: CharArray,
    ): LoadedKeyStore {
        val normalizedType = normalizeKeyStoreType(preferredType)
        val keyStore = try {
            KeyStore.getInstance(normalizedType)
        } catch (throwable: Throwable) {
            throw IllegalArgumentException("无法创建 $normalizedType 密钥库，请确认类型是否正确", throwable)
        }
        try {
            keyStore.load(keyStoreBytes.inputStream(), storePassword)
        } catch (throwable: Throwable) {
            throw IllegalArgumentException(
                when (normalizedType) {
                    "JKS" -> "JKS 密钥读取失败，请确认文件确实是 JKS 格式，并检查密码和别名"
                    "PKCS12" -> "PKCS12 密钥读取失败，请确认文件格式、密码和别名"
                    "BKS" -> "BKS 密钥读取失败，请确认文件格式、密码和别名"
                    else -> "密钥文件读取失败，请确认格式、密码和别名"
                },
                throwable,
            )
        }
        return LoadedKeyStore(type = normalizedType, keyStore = keyStore)
    }
}
