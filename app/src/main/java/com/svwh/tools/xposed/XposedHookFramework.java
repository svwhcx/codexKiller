package com.svwh.tools.xposed;

import com.svwh.noenvhook.core.MethodHookInvocation;
import com.svwh.noenvhook.framework.DefaultHookHelpers;
import com.svwh.noenvhook.framework.HookBridge;
import com.svwh.noenvhook.framework.HookCallback;
import com.svwh.noenvhook.framework.HookFramework;
import com.svwh.noenvhook.framework.HookHelpers;
import com.svwh.noenvhook.framework.HookToolkit;

import java.lang.reflect.Member;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;

final class XposedHookFramework implements HookFramework {

    private final HookBridge bridge = new XposedRuntimeBridge(this);
    private final HookHelpers helpers = new DefaultHookHelpers(bridge);
    private final HookToolkit toolkit = new HookToolkit(this, bridge, helpers);

    @Override
    public void hook(Member target, HookCallback callback) {
        XposedBridge.hookMethod(target, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    callback.beforeCall(new XposedCallFrame(param));
                } catch (Throwable throwable) {
                    bridge.log(throwable);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    callback.afterCall(new XposedCallFrame(param));
                } catch (Throwable throwable) {
                    bridge.log(throwable);
                }
            }
        });
    }

    @Override
    public void hookReplacement(Member target, MethodHookInvocation invocation) {
        XposedBridge.hookMethod(target, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                XposedCallFrame hookCallFrame = new XposedCallFrame(param);
                try {
                    return invocation.replaceMethodHook(hookCallFrame);
                } catch (Throwable throwable) {
                    bridge.log(throwable);
                    return hookCallFrame.invokeOriginal();
                }
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
