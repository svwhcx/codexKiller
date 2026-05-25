package com.svwh.noenvhook.core.web;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.LogInvocation;
import com.svwh.noenvhook.framework.HookCallFrame;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.StackTraceCollector;


public class VpnHookInvocation extends LogInvocation {

    public VpnHookInvocation(
            HookConfig hookConfig,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        super(hookConfig, logWriter, stackTraceCollector);
    }

    @Override
    public Object replaceMethodHook(HookCallFrame callFrame) {
        try {
            Object result = callFrame.invokeOriginal();
            String name = (String) callFrame.getArg(0);
            if (name.startsWith("tun") || name.startsWith("ppp")
                    || name.startsWith("tap") || name.startsWith("vpn")) {
                return null;
            }
            return result;
        } catch (Throwable e) {
            return null;
        }
    }
}
