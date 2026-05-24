package com.svwh.noenvhook.core.other;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.management.HookConfigTypeEnum;
import com.svwh.noenvhook.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;

public class SignatureHookInvocation {

    public static void hook(ClassLoader classLoader, HookLogWriter logWriter) throws Exception {
        @SuppressLint("PrivateApi")
        Method member = classLoader.loadClass("android.app.ApplicationPackageManager")
                .getDeclaredMethod("getPackageInfo", String.class, int.class);
        Pine.hook(member, new MethodHook() {
            @Override
            public void afterCall(Pine.CallFrame callFrame) throws Throwable {
                super.afterCall(callFrame);
                PackageInfo packageInfo = (PackageInfo) callFrame.getResult();
                int value = (int) callFrame.args[1];
                if (packageInfo == null || packageInfo.signatures == null || packageInfo.signatures.length == 0) {
                    return;
                }
                if (value == PackageManager.GET_SIGNATURES) {
                    byte[] signaturesByteArray = packageInfo.signatures[0].toByteArray();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(signaturesByteArray);
                    X509Certificate x509Certificate = (X509Certificate) CertificateFactory.getInstance("X509")
                            .generateCertificate(byteArrayInputStream);

                    boolean addColon = false;
                    boolean isUpperCase = false;

                    String encodeMd5 = StringUtils.toHexString(
                            MessageDigest.getInstance("MD5").digest(x509Certificate.getEncoded()),
                            addColon,
                            isUpperCase
                    );
                    String encodeSha1 = StringUtils.toHexString(
                            MessageDigest.getInstance("SHA1").digest(x509Certificate.getEncoded()),
                            addColon,
                            isUpperCase
                    );
                    String encodeSha256 = StringUtils.toHexString(
                            MessageDigest.getInstance("SHA256").digest(x509Certificate.getEncoded()),
                            addColon,
                            isUpperCase
                    );
                    String encodeBase64 = Base64.encodeToString(signaturesByteArray, 0);

                    Log log = new Log();
                    log.setTitle("读取应用签名监听");
                    log.setType(HookConfigTypeEnum.SIGNATURE);
                    StringBuilder sb = new StringBuilder();
                    sb.append("签名结果(md5):").append(encodeMd5).append("\n");
                    sb.append("签名结果(sha1):").append(encodeSha1).append("\n");
                    sb.append("签名结果(sha256):").append(encodeSha256).append("\n");
                    sb.append("签名结果(base64):").append(encodeBase64).append("\n");
                    log.setContent(sb.toString());
                    logWriter.save(log);
                }
            }
        });
    }
}
