package com.svwh.noenvhook.framework.pine;

import com.svwh.noenvhook.core.MethodHookInvocation;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;

import java.lang.reflect.Member;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;
import top.canyie.pine.callback.MethodReplacement;

public class PineHookFramework implements HookFramework {

    @Override
    public void hook(Member target, HookCallback callback) {
        Pine.hook(target, new MethodHook() {
            @Override
            public void beforeCall(Pine.CallFrame callFrame) throws Throwable {
                super.beforeCall(callFrame);
                callback.beforeCall(new PineCallFrame(callFrame));
            }

            @Override
            public void afterCall(Pine.CallFrame callFrame) throws Throwable {
                super.afterCall(callFrame);
                callback.afterCall(new PineCallFrame(callFrame));
            }
        });
    }

    @Override
    public void hookReplacement(Member target, MethodHookInvocation invocation) {
        Pine.hook(target, new MethodReplacement() {
            @Override
            protected Object replaceCall(Pine.CallFrame callFrame) throws Throwable {
                return invocation.replaceMethodHook(new PineCallFrame(callFrame));
            }
        });
    }
}
