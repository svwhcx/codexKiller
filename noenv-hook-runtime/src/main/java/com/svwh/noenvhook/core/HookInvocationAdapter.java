package com.svwh.noenvhook.core;

import top.canyie.pine.Pine;
import top.canyie.pine.callback.MethodHook;

/**
 * @description
 * @Author chenxin
 * @Date 2025/4/26 16:55
 */
public class HookInvocationAdapter extends MethodHook {

    private final MethodHookInvocation methodHookInvocation;

    public HookInvocationAdapter(MethodHookInvocation methodHookInvocation) {
        this.methodHookInvocation = methodHookInvocation;
    }

    @Override
    public void beforeCall(Pine.CallFrame callFrame) throws Throwable {
        super.beforeCall(callFrame);
        methodHookInvocation.beforeMethodHook(callFrame);
    }

    @Override
    public void afterCall(Pine.CallFrame callFrame) throws Throwable {
        super.afterCall(callFrame);
        methodHookInvocation.afterMethodHook(callFrame);
    }
}
