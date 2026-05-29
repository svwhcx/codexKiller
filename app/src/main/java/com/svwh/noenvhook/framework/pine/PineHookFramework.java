package com.svwh.noenvhook.framework.pine;

import com.svwh.noenvhook.core.MethodHookInvocation;
import com.svwh.noenvhook.framework.DefaultHookBridge;
import com.svwh.noenvhook.framework.DefaultHookHelpers;
import com.svwh.noenvhook.framework.HookBridge;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.framework.HookHelpers;
import com.svwh.noenvhook.framework.HookToolkit;

import java.lang.reflect.Member;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;
import top.canyie.pine.callback.MethodReplacement;

public class PineHookFramework implements HookFramework {

    private final HookBridge bridge = new DefaultHookBridge(this);
    private final HookHelpers helpers = new DefaultHookHelpers(bridge);
    private final HookToolkit toolkit = new HookToolkit(this, bridge, helpers);

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

    @Override
    public HookBridge bridge() {
        return bridge;
    }

    @Override
    public HookHelpers helpers() {
        return helpers;
    }

    @Override
    public HookToolkit toolkit() {
        return toolkit;
    }
}
