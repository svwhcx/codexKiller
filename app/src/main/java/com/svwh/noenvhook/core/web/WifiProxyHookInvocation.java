package com.svwh.noenvhook.core.web;

import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.io.IOException;
import java.lang.reflect.Member;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public final class WifiProxyHookInvocation {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ThreadLocal<Boolean> WRITING_LOG = new ThreadLocal<>();
    private static final ProxySelector DIRECT_SELECTOR = new ProxySelector() {
        @Override
        public List<Proxy> select(URI uri) {
            return Collections.singletonList(Proxy.NO_PROXY);
        }

        @Override
        public void connectFailed(URI uri, java.net.SocketAddress sa, IOException ioe) {
        }
    };

    private WifiProxyHookInvocation() {
    }

    public static void hook(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookSystemProperties(hookFramework, logWriter, stackTraceCollector);
        hookProxySelector(hookFramework, logWriter, stackTraceCollector);
        hookAndroidProxyClass(hookFramework, logWriter, stackTraceCollector);
        hookInetSocketAddress(hookFramework, logWriter, stackTraceCollector);
    }

    private static void hookSystemProperties(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookFramework.hook(System.class.getDeclaredMethod("getProperty", String.class), new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) {
                String key = (String) callFrame.getArg(0);
                if (isProxyProperty(key) && callFrame.getResult() != null) {
                    Object original = callFrame.getResult();
                    callFrame.setResult(null);
                    saveLog(logWriter, stackTraceCollector, "隐藏 Wifi 代理属性", "key: " + key + "\noriginal: " + original + "\nreplacement: null\n");
                }
            }
        });
        hookFramework.hook(System.class.getDeclaredMethod("getProperty", String.class, String.class), new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) {
                String key = (String) callFrame.getArg(0);
                if (isProxyProperty(key)) {
                    Object defaultValue = callFrame.getArg(1);
                    Object original = callFrame.getResult();
                    callFrame.setResult(defaultValue);
                    saveLog(logWriter, stackTraceCollector, "隐藏 Wifi 代理属性", "key: " + key + "\noriginal: " + original + "\nreplacement: " + defaultValue + "\n");
                }
            }
        });
    }

    private static void hookProxySelector(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookFramework.hook(ProxySelector.class.getDeclaredMethod("getDefault"), new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) {
                Object original = callFrame.getResult();
                callFrame.setResult(DIRECT_SELECTOR);
                saveLog(logWriter, stackTraceCollector, "隐藏默认代理选择器", "original: " + original + "\nreplacement: DIRECT\n");
            }
        });
    }

    private static void hookAndroidProxyClass(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        tryHookStaticMethod(hookFramework, logWriter, stackTraceCollector, "android.net.Proxy", "getDefaultHost", null);
        tryHookStaticMethod(hookFramework, logWriter, stackTraceCollector, "android.net.Proxy", "getDefaultPort", -1);
        tryHookStaticMethod(hookFramework, logWriter, stackTraceCollector, "android.net.Proxy", "getHost", null, android.content.Context.class);
        tryHookStaticMethod(hookFramework, logWriter, stackTraceCollector, "android.net.Proxy", "getPort", -1, android.content.Context.class);
    }

    private static void hookInetSocketAddress(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookFramework.hook(InetSocketAddress.class.getDeclaredMethod("getHostName"), new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) {
                Object thisObject = callFrame.getThisObject();
                Object result = callFrame.getResult();
                if (thisObject instanceof InetSocketAddress
                        && result instanceof String
                        && isLikelyProxyAddress((InetSocketAddress) thisObject)) {
                    callFrame.setResult("");
                    saveLog(logWriter, stackTraceCollector, "隐藏代理地址", "originalHost: " + result + "\nreplacement: empty\n");
                }
            }
        });
        hookFramework.hook(InetSocketAddress.class.getDeclaredMethod("getPort"), new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) {
                Object thisObject = callFrame.getThisObject();
                if (thisObject instanceof InetSocketAddress
                        && isLikelyProxyAddress((InetSocketAddress) thisObject)) {
                    Object original = callFrame.getResult();
                    callFrame.setResult(-1);
                    saveLog(logWriter, stackTraceCollector, "隐藏代理端口", "originalPort: " + original + "\nreplacement: -1\n");
                }
            }
        });
    }

    private static void tryHookStaticMethod(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector,
            String className,
            String methodName,
            Object replacement,
            Class<?>... parameterTypes
    ) {
        try {
            Class<?> targetClass = Class.forName(className);
            Member method = targetClass.getDeclaredMethod(methodName, parameterTypes);
            hookFramework.hook(method, new HookCallback() {
                @Override
                public void afterCall(HookCallFrame callFrame) {
                    Object original = callFrame.getResult();
                    callFrame.setResult(replacement);
                    saveLog(logWriter, stackTraceCollector, "隐藏 Android Wifi 代理", "method: " + className + "#" + methodName + "\noriginal: " + original + "\nreplacement: " + replacement + "\n");
                }
            });
        } catch (Throwable ignored) {
        }
    }

    private static boolean isProxyProperty(String key) {
        if (key == null) {
            return false;
        }
        String lower = key.toLowerCase();
        return lower.contains("proxyhost")
                || lower.contains("proxyport")
                || lower.contains("proxyset")
                || lower.contains("nonproxyhosts");
    }

    private static boolean isLikelyProxyAddress(InetSocketAddress address) {
        int port = address.getPort();
        return port == 8888 || port == 8080 || port == 8081 || port == 8082
                || port == 9090 || port == 8000 || port == 8889;
    }

    private static void saveLog(
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector,
            String title,
            String content
    ) {
        if (Boolean.TRUE.equals(WRITING_LOG.get())) {
            return;
        }
        try {
            WRITING_LOG.set(Boolean.TRUE);
            Log log = new Log();
            log.setType(HookConfigTypeEnum.WIFI_PROXY);
            log.setTitle(title);
            log.setTime(LocalDateTime.now().format(TIME_FORMATTER));
            log.setStackTrace(stackTraceCollector.collect());
            log.setContent(content);
            logWriter.save(log);
        } finally {
            WRITING_LOG.remove();
        }
    }
}
