package com.svwh.tools.apk

import com.android.apksig.ApkSigner
import java.io.File
import java.io.InputStream
import java.security.Key
import java.security.KeyStore
import java.security.cert.X509Certificate
import java.util.Collections


/**
 * @description
 * @Author chenxin
 * @Date 2024/11/28 12:33
 */
object SignUtils {
    /**
     * 对apk进行签名
     */
    public fun signApk(key: InputStream, password: String, inputApk: File, output: File) {
        signApk(key, password, inputApk, output, true, true, true);
    }

    private fun signApk(
        key: InputStream,
        password: String,
        inputApk: File,
        output: File,
        v1: Boolean,
        v2: Boolean,
        v3: Boolean
    ) {
        val pw = password.toCharArray()
        val keystore = KeyStore.getInstance("BKS");
        keystore.load(key, pw);
        val alias = keystore.aliases().nextElement()
        val entry = keystore.getEntry(
            alias,
            KeyStore.PasswordProtection("killer.svwh".toCharArray()),
        ) as KeyStore.PrivateKeyEntry
        val certificate = keystore.getCertificate(alias) as X509Certificate
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
                    alias = alias,
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
            appendLine("APK大小：${inputApk.length()} bytes")
            appendLine("alias：$alias")
            appendLine("私钥算法：${privateKey.algorithm}")
            appendLine("证书算法：${certificate.sigAlgName}")
            appendLine("签名方案：v1=$v1, v2=$v2, v3=$v3")
        }
    }

   /* public fun newSignApk(){
        ApkSigner.Builder()
    }*/
}
