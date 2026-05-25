package com.svwh.noenvhook.core.crypto;

import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.lang.reflect.Member;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class UserCertificateTrustHookInvocation {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ThreadLocal<Boolean> WRITING_LOG = new ThreadLocal<>();

    private UserCertificateTrustHookInvocation() {
    }

    public static void hook(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        Member init = SSLContext.class.getDeclaredMethod(
                "init",
                KeyManager[].class,
                TrustManager[].class,
                SecureRandom.class
        );
        hookFramework.hook(init, new HookCallback() {
            @Override
            public void beforeCall(HookCallFrame callFrame) {
                if (isWritingLog()) {
                    return;
                }
                TrustManager[] trustManagers = buildAndroidCaStoreTrustManagers();
                if (trustManagers == null || trustManagers.length == 0) {
                    saveLog(
                            logWriter,
                            stackTraceCollector,
                            "用户证书信任失败",
                            "reason: AndroidCAStore TrustManager unavailable\n"
                                    + "sslContext: " + describeSslContext(callFrame.getThisObject()) + "\n"
                    );
                    return;
                }

                TrustManager[] original = (TrustManager[]) callFrame.getArg(1);
                callFrame.setArg(1, trustManagers);
                saveLog(
                        logWriter,
                        stackTraceCollector,
                        "已启用用户证书信任",
                        "sslContext: " + describeSslContext(callFrame.getThisObject()) + "\n"
                                + "originalTrustManagers: " + describeTrustManagers(original) + "\n"
                                + "replacementTrustManagers: " + describeTrustManagers(trustManagers) + "\n"
                                + "source: AndroidCAStore\n"
                );
            }
        });
    }

    private static TrustManager[] buildAndroidCaStoreTrustManagers() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidCAStore");
            keyStore.load(null);
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(keyStore);
            return factory.getTrustManagers();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String describeSslContext(Object value) {
        if (value instanceof SSLContext) {
            SSLContext context = (SSLContext) value;
            return context.getProtocol() + "/" + context.getProvider().getName();
        }
        return String.valueOf(value);
    }

    private static String describeTrustManagers(TrustManager[] trustManagers) {
        if (trustManagers == null || trustManagers.length == 0) {
            return "empty";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < trustManagers.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            TrustManager trustManager = trustManagers[i];
            builder.append(trustManager.getClass().getName());
            if (trustManager instanceof X509TrustManager) {
                X509TrustManager x509 = (X509TrustManager) trustManager;
                builder.append("(acceptedIssuers=").append(x509.getAcceptedIssuers().length).append(")");
            }
        }
        return builder.toString();
    }

    private static void saveLog(
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector,
            String title,
            String content
    ) {
        try {
            WRITING_LOG.set(Boolean.TRUE);
            Log log = new Log();
            log.setType(HookConfigTypeEnum.USER_CERT_TRUST);
            log.setTitle(title);
            log.setTime(LocalDateTime.now().format(TIME_FORMATTER));
            log.setStackTrace(stackTraceCollector.collect());
            log.setContent(content);
            logWriter.save(log);
        } finally {
            WRITING_LOG.remove();
        }
    }

    private static boolean isWritingLog() {
        return Boolean.TRUE.equals(WRITING_LOG.get());
    }
}
