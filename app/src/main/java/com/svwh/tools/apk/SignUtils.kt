package com.svwh.tools.apk

import com.android.apksig.ApkSigner
import java.io.File
import java.io.InputStream
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
        ApkSigner.Builder(
            Collections.singletonList(
                ApkSigner.SignerConfig.Builder(
                    "CERT",
                    (keystore.getEntry(
                        alias,
                        KeyStore.PasswordProtection("killer.svwh".toCharArray())
                    ) as KeyStore.PrivateKeyEntry).privateKey,
                    Collections.singletonList(keystore.getCertificate(alias) as X509Certificate)
                ).build()
            )
        )
            .setInputApk(inputApk)
            .setOutputApk(output)
            .setCreatedBy("Android Gradle 8.0.2")
            .setV1SigningEnabled(v1)
            .setV2SigningEnabled(v2)
            .setV3SigningEnabled(v3).build().sign();
    }

   /* public fun newSignApk(){
        ApkSigner.Builder()
    }*/
}