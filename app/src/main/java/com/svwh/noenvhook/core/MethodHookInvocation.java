package com.svwh.noenvhook.core;

import com.svwh.noenvhook.framework.HookCallFrame;

public interface MethodHookInvocation {

    void beforeMethodHook(HookCallFrame callFrame);

    void afterMethodHook(HookCallFrame callFrame);

    Object replaceMethodHook(HookCallFrame callFrame) throws Throwable;

    void changeStaticField(HookCallFrame callFrame);

    void changeField(HookCallFrame callFrame);

    void build();
}
