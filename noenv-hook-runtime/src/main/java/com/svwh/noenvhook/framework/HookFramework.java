package com.svwh.noenvhook.framework;

import com.svwh.noenvhook.core.MethodHookInvocation;

import java.lang.reflect.Member;

public interface HookFramework {

    void hook(Member target, HookCallback callback);

    void hookReplacement(Member target, MethodHookInvocation invocation);
}
