package com.svwh.noenvhook.core.web;

import android.net.NetworkCapabilities;

import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.Log;
import com.svwh.noenvhook.log.StackTraceCollector;
import com.svwh.noenvhook.management.HookConfigTypeEnum;

import java.lang.reflect.Member;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class VpnHookInvocation {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ThreadLocal<Boolean> WRITING_LOG = new ThreadLocal<>();

    private VpnHookInvocation() {
    }

    public static void hook(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookNetworkInterfaceName(
                hookFramework,
                logWriter,
                stackTraceCollector,
                NetworkInterface.class.getDeclaredMethod("getName")
        );
        hookNetworkInterfaceName(
                hookFramework,
                logWriter,
                stackTraceCollector,
                NetworkInterface.class.getDeclaredMethod("getDisplayName")
        );
        hookNetworkCapabilities(hookFramework, logWriter, stackTraceCollector);
    }

    private static void hookNetworkInterfaceName(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector,
            Member member
    ) {
        hookFramework.hook(member, new HookCallback() {
            @Override
            public void afterCall(HookCallFrame callFrame) {
                Object result = callFrame.getResult();
                if (result instanceof String && isVpnInterface((String) result)) {
                    callFrame.setResult("wlan0");
                    saveLog(
                            logWriter,
                            stackTraceCollector,
                            "隐藏 VPN 网络接口",
                            "method: " + callFrame.getMethodName() + "\n"
                                    + "original: " + result + "\n"
                                    + "replacement: wlan0\n"
                    );
                }
            }
        });
    }

    private static void hookNetworkCapabilities(
            HookFramework hookFramework,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) throws NoSuchMethodException {
        hookFramework.hook(
                NetworkCapabilities.class.getDeclaredMethod("hasTransport", int.class),
                new HookCallback() {
                    @Override
                    public void afterCall(HookCallFrame callFrame) {
                        Object transport = callFrame.getArg(0);
                        if (transport instanceof Integer
                                && ((Integer) transport) == NetworkCapabilities.TRANSPORT_VPN
                                && Boolean.TRUE.equals(callFrame.getResult())) {
                            callFrame.setResult(false);
                            saveLog(
                                    logWriter,
                                    stackTraceCollector,
                                    "隐藏 VPN Transport",
                                    "method: NetworkCapabilities.hasTransport\n"
                                            + "transport: VPN\n"
                                            + "replacement: false\n"
                            );
                        }
                    }
                }
        );
    }

    private static boolean isVpnInterface(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase();
        return lower.startsWith("tun")
                || lower.startsWith("tap")
                || lower.startsWith("ppp")
                || lower.contains("vpn")
                || lower.contains("wg")
                || lower.contains("utun");
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
            log.setType(HookConfigTypeEnum.VPN);
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
