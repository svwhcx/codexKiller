package com.svwh.noenvhook.core.web;

import com.svwh.noenvhook.conf.HookConfig;
import com.svwh.noenvhook.core.LogInvocation;
import com.svwh.noenvhook.log.HookLogWriter;
import com.svwh.noenvhook.log.StackTraceCollector;

import top.canyie.pine.Pine;

public class VpnHookInvocation extends LogInvocation {

    public VpnHookInvocation(
            HookConfig hookConfig,
            HookLogWriter logWriter,
            StackTraceCollector stackTraceCollector
    ) {
        super(hookConfig, logWriter, stackTraceCollector);
    }

    @Override
    public Object replaceMethodHook(Pine.CallFrame callFrame) {
        try {
            Object result = callFrame.invokeOriginalMethod();
            String name = (String) callFrame.args[0];
            if (name.startsWith("tun") || name.startsWith("ppp")
                    || name.startsWith("tap") || name.startsWith("vpn")) {
                return null;
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
