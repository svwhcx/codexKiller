package com.svwh.tools.apk

import com.android.apksig.ApkSigner
import java.io.File
import java.io.InputStream
import java.security.Key
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.Collections

/**
 * APK signing utilities.
 */
object SignUtils {
    fun signApk(key: InputStream, password: String, inputApk: File, output: File) {
        signApk(
            key = key,
            keyStoreType = "BKS",
            storePassword = password,
            keyPassword = "killer.svwh",
            alias = null,
            inputApk = inputApk,
            output = output,
        )
    }

    fun signApk(
        key: InputStream,
        keyStoreType: String,
        storePassword: String,
        keyPassword: String,
        alias: String?,
        inputApk: File,
        output: File,
    ) {
        signApk(
            key = key,
            keyStoreType = keyStoreType,
            storePassword = storePassword,
            keyPassword = keyPassword,
            alias = alias,
            inputApk = inputApk,
            output = output,
            v1 = true,
            v2 = true,
            v3 = true,
        )
    }

    private fun signApk(
        key: InputStream,
        keyStoreType: String,
        storePassword: String,
        keyPassword: String,
        alias: String?,
        inputApk: File,
        output: File,
        v1: Boolean,
        v2: Boolean,
        v3: Boolean,
    ) {
        val loadedKeyStore = SigningKeyStoreUtils.loadKeyStore(
            keyStoreBytes = key.readBytes(),
            preferredType = keyStoreType,
            storePassword = storePassword.toCharArray(),
        )
        val keystore = loadedKeyStore.keyStore
        val aliases = keystore.aliases()
        val resolvedAlias = alias
            ?.takeIf { it.isNotBlank() }
            ?: if (aliases.hasMoreElements()) {
                aliases.nextElement()
            } else {
                throw IllegalStateException("密钥库中没有可用别名")
            }
        val entry = runCatching {
            keystore.getEntry(
                resolvedAlias,
                KeyStore.PasswordProtection(keyPassword.toCharArray()),
            )
        }.getOrElse {
            throw IllegalStateException("读取签名私钥失败，请检查别名密码是否正确", it)
        } as? KeyStore.PrivateKeyEntry
            ?: throw IllegalStateException("选择的别名不是私钥条目")
        val certificate = keystore.getCertificate(resolvedAlias) as? X509Certificate
            ?: throw IllegalStateException("签名证书不是 X509 证书")

        runCatching {
            ApkSigner.Builder(
                Collections.singletonList(
                    ApkSigner.SignerConfig.Builder(
                        "CERT",
                        entry.privateKey,
                        Collections.singletonList(certificate),
                    ).build(),
                ),
            )
                .setInputApk(inputApk)
                .setOutputApk(output)
                .setCreatedBy("Android Gradle 8.0.2")
                .setV1SigningEnabled(v1)
                .setV2SigningEnabled(v2)
                .setV3SigningEnabled(v3)
                .build()
                .sign()
        }.getOrElse { throwable ->
            output.delete()
            throw IllegalStateException(
                buildSignErrorMessage(
                    throwable = throwable,
                    inputApk = inputApk,
                    alias = resolvedAlias,
                    privateKey = entry.privateKey,
                    certificate = certificate,
                    v1 = v1,
                    v2 = v2,
                    v3 = v3,
                ),
                throwable,
            )
        }
    }

    private fun buildSignErrorMessage(
        throwable: Throwable,
        inputApk: File,
        alias: String,
        privateKey: Key,
        certificate: X509Certificate,
        v1: Boolean,
        v2: Boolean,
        v3: Boolean,
    ): String {
        return buildString {
            appendLine("APK 签名失败")
            appendLine("原因：${throwable.message ?: throwable::class.java.name}")
            appendLine("APK：${inputApk.absolutePath}")
            appendLine("APK 大小：${inputApk.length()} bytes")
            appendLine("别名：$alias")
            appendLine("私钥算法：${privateKey.algorithm}")
            appendLine("证书算法：${certificate.sigAlgName}")
            appendLine("签名方案：v1=$v1, v2=$v2, v3=$v3")
        }
    }
}
